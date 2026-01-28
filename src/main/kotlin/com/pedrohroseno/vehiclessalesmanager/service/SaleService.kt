package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Sale
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat

@Service
class SaleService(
    private val saleRepository: SaleRepository,
    private val partnerService: PartnerService,
    private val vehicleService: VehicleService,
    private val transactionHistoryService: TransactionHistoryService
) {
    fun getAllSales(pageable: Pageable, search: String? = null): Page<SaleResponseDTO> {
        return if (search.isNullOrBlank()) {
            saleRepository.findAllByDeletedFalseOrderBySaleDateDesc(pageable).map { it.toResponseDTO() }
        } else {
            saleRepository.searchByVehicleOrPartnerAndDeletedFalse(search.trim(), pageable).map { it.toResponseDTO() }
        }
    }

    @Transactional
    fun createSale(dto: SaleCreateDTO): Sale {
        // Buscar parceiro (comprador)
        val partner = partnerService.findByDocument(dto.customer.document)
            ?: throw IllegalArgumentException("Parceiro não encontrado. Cadastre o parceiro primeiro.")

        // Buscar veículo
        val vehicle = vehicleService.findByLicensePlate(dto.vehicle.licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: ${dto.vehicle.licensePlate}")

        // Verificar se o veículo está disponível
        if (!vehicleService.isAvailable(dto.vehicle.licensePlate)) {
            throw IllegalStateException("Veículo não está disponível para venda: ${dto.vehicle.licensePlate}")
        }

        // Criar a venda
        val sale = Sale(
            vehicle = vehicle,
            partner = partner,
            salePrice = dto.salePrice,
            saleDate = java.util.Date()
        )

        val savedSale = saleRepository.save(sale)

        // Atualizar status do veículo para VENDIDO (regra de negócio)
        vehicleService.updateVehicleStatus(dto.vehicle.licensePlate, VehicleStatus.VENDIDO)

        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.SALE,
            transactionId = savedSale.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            actionType = ActionType.CREATED,
            description = "Venda criada: R$ ${dto.salePrice} - Veículo: ${vehicle.licensePlate}",
            performedBy = null
        )

        return savedSale
    }

    @Transactional
    fun updateSale(id: Long, dto: SaleUpdateDTO, performedBy: String? = null): Sale {
        val sale = saleRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Venda não encontrada ou já excluída: $id")
        
        if (sale.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Não é possível editar uma venda cancelada")
        }
        
        val oldPrice = sale.salePrice
        val oldDate = sale.saleDate
        
        // Atualizar preço se fornecido
        if (dto.salePrice != null && dto.salePrice > 0) {
            sale.salePrice = dto.salePrice
        }
        
        // Atualizar data se fornecida
        if (!dto.saleDate.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                sale.saleDate = dateFormat.parse(dto.saleDate)
            } catch (e: Exception) {
                throw IllegalArgumentException("Data inválida: ${dto.saleDate}")
            }
        }
        
        val updatedSale = saleRepository.save(sale)
        
        // Log histórico
        val description = buildString {
            if (dto.salePrice != null && dto.salePrice != oldPrice) {
                append("Valor alterado de R$ $oldPrice para R$ ${dto.salePrice}. ")
            }
            if (!dto.saleDate.isNullOrBlank()) {
                append("Data atualizada. ")
            }
            append("Saldo atualizado.")
        }
        
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.SALE,
            transactionId = updatedSale.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            actionType = ActionType.EDITED,
            description = "Venda editada: $description",
            oldValue = "Preço: R$ $oldPrice, Data: $oldDate",
            newValue = "Preço: R$ ${updatedSale.salePrice}, Data: ${updatedSale.saleDate}",
            performedBy = performedBy
        )
        
        return updatedSale
    }

    @Transactional
    fun cancelSale(id: Long, performedBy: String? = null): Sale {
        val sale = saleRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Venda não encontrada ou já excluída: $id")
        
        if (sale.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Venda já está cancelada")
        }
        
        sale.status = TransactionStatus.CANCELLED
        val cancelledSale = saleRepository.save(sale)
        
        // Reverter status do veículo para DISPONIVEL
        vehicleService.updateVehicleStatus(sale.vehicle.licensePlate, VehicleStatus.DISPONIVEL)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.SALE,
            transactionId = cancelledSale.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            actionType = ActionType.CANCELLED,
            description = "Venda cancelada: R$ ${sale.salePrice} - Veículo: ${sale.vehicle.licensePlate}. Status do veículo revertido para DISPONIVEL.",
            performedBy = performedBy
        )
        
        return cancelledSale
    }

    @Transactional
    fun deleteSale(id: Long) {
        // deleteSale agora chama cancelSale para manter consistência
        cancelSale(id, null)
    }

    private fun Sale.toResponseDTO(): SaleResponseDTO {
        return SaleResponseDTO(
            id = this.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            vehicleBrand = this.vehicle.brand.name,
            vehicleModel = this.vehicle.modelName,
            partnerDocument = this.partner.document,
            partnerName = this.partner.name,
            salePrice = this.salePrice,
            saleDate = this.saleDate,
            status = this.status
        )
    }
}
