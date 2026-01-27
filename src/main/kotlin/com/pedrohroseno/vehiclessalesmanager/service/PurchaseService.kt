package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Purchase
import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.PurchaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.util.Date

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val partnerService: PartnerService,
    private val vehicleService: VehicleService,
    private val transactionHistoryService: TransactionHistoryService
) {
    fun getAllPurchases(pageable: Pageable, search: String? = null): Page<PurchaseResponseDTO> {
        return if (search.isNullOrBlank()) {
            purchaseRepository.findAllByDeletedFalseOrderByPurchaseDateDesc(pageable).map { it.toResponseDTO() }
        } else {
            purchaseRepository.searchByVehicleOrPartnerAndDeletedFalse(search.trim(), pageable).map { it.toResponseDTO() }
        }
    }
    @Transactional
    fun createPurchase(dto: PurchaseCreateDTO): Purchase {
        // Buscar ou criar parceiro (fornecedor)
        val partner = partnerService.findByCpf(dto.customer.cpf)
            ?: throw IllegalArgumentException("Parceiro não encontrado. Cadastre o parceiro primeiro.")

        // Buscar veículo - deve existir (regra: na compra, o veículo é cadastrado automaticamente)
        // Se não existir, significa que precisa ser cadastrado primeiro via endpoint de veículos
        val existingVehicle = vehicleService.findByLicensePlate(dto.vehicle.licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: ${dto.vehicle.licensePlate}. Cadastre o veículo primeiro.")

        // Regra de negócio: Sempre que uma "Compra" for registrada, o sistema deve cadastrar 
        // automaticamente o veículo no estoque com o status DISPONIVEL
        // Se o veículo já existe, apenas atualiza o status para DISPONIVEL
        existingVehicle.status = VehicleStatus.DISPONIVEL
        val vehicle = vehicleService.saveVehicle(existingVehicle)

        // Parse da data
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val purchaseDate = try {
            dateFormat.parse(dto.purchaseDate)
        } catch (e: Exception) {
            Date() // Se falhar, usa a data atual
        }

        // Criar a compra
        val purchase = Purchase(
            vehicle = vehicle,
            partner = partner,
            purchasePrice = dto.purchasePrice,
            purchaseDate = purchaseDate
        )

        val savedPurchase = purchaseRepository.save(purchase)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.PURCHASE,
            transactionId = savedPurchase.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            actionType = ActionType.CREATED,
            description = "Compra criada: R$ ${dto.purchasePrice} - Veículo: ${vehicle.licensePlate}",
            performedBy = null
        )
        
        return savedPurchase
    }

    /**
     * Método auxiliar para criar compra com veículo completo
     * Usado quando o veículo ainda não existe no sistema
     */
    @Transactional
    fun createPurchaseWithVehicle(dto: PurchaseCreateDTO, vehicleDto: VehicleCreateDTO): Purchase {
        // Buscar ou criar parceiro
        val partner = partnerService.findByCpf(dto.customer.cpf)
            ?: throw IllegalArgumentException("Parceiro não encontrado. Cadastre o parceiro primeiro.")

        // Criar veículo com status DISPONIVEL (regra de negócio)
        val vehicle = vehicleService.createVehicle(
            vehicleDto.copy(inStock = true) // Garante que está disponível
        )

        // Parse da data
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val purchaseDate = try {
            dateFormat.parse(dto.purchaseDate)
        } catch (e: Exception) {
            Date()
        }

        // Criar a compra
        val purchase = Purchase(
            vehicle = vehicle,
            partner = partner,
            purchasePrice = dto.purchasePrice,
            purchaseDate = purchaseDate
        )

        val savedPurchase = purchaseRepository.save(purchase)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.PURCHASE,
            transactionId = savedPurchase.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            actionType = ActionType.CREATED,
            description = "Compra criada: R$ ${dto.purchasePrice} - Veículo: ${vehicle.licensePlate}",
            performedBy = null
        )
        
        return savedPurchase
    }

    @Transactional
    fun updatePurchase(id: Long, dto: PurchaseUpdateDTO, performedBy: String? = null): Purchase {
        val purchase = purchaseRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Compra não encontrada ou já excluída: $id")
        
        if (purchase.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Não é possível editar uma compra cancelada")
        }
        
        val oldPrice = purchase.purchasePrice
        val oldDate = purchase.purchaseDate
        
        // Atualizar preço se fornecido
        if (dto.purchasePrice != null && dto.purchasePrice > 0) {
            purchase.purchasePrice = dto.purchasePrice
        }
        
        // Atualizar data se fornecida
        if (!dto.purchaseDate.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                purchase.purchaseDate = dateFormat.parse(dto.purchaseDate)
            } catch (e: Exception) {
                throw IllegalArgumentException("Data inválida: ${dto.purchaseDate}")
            }
        }
        
        val updatedPurchase = purchaseRepository.save(purchase)
        
        // Log histórico
        val description = buildString {
            if (dto.purchasePrice != null && dto.purchasePrice != oldPrice) {
                append("Valor alterado de R$ $oldPrice para R$ ${dto.purchasePrice}. ")
            }
            if (!dto.purchaseDate.isNullOrBlank()) {
                append("Data atualizada. ")
            }
            append("Saldo atualizado.")
        }
        
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.PURCHASE,
            transactionId = updatedPurchase.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            actionType = ActionType.EDITED,
            description = "Compra editada: $description",
            oldValue = "Preço: R$ $oldPrice, Data: $oldDate",
            newValue = "Preço: R$ ${updatedPurchase.purchasePrice}, Data: ${updatedPurchase.purchaseDate}",
            performedBy = performedBy
        )
        
        return updatedPurchase
    }

    @Transactional
    fun cancelPurchase(id: Long, performedBy: String? = null): Purchase {
        val purchase = purchaseRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Compra não encontrada ou já excluída: $id")
        
        if (purchase.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Compra já está cancelada")
        }
        
        purchase.status = TransactionStatus.CANCELLED
        val cancelledPurchase = purchaseRepository.save(purchase)
        
        // Marcar veículo como INACTIVE quando compra é cancelada
        vehicleService.updateVehicleStatus(purchase.vehicle.licensePlate, VehicleStatus.INACTIVE)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.PURCHASE,
            transactionId = cancelledPurchase.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            actionType = ActionType.CANCELLED,
            description = "Compra cancelada: R$ ${purchase.purchasePrice} - Veículo: ${purchase.vehicle.licensePlate}. Status do veículo alterado para INACTIVE.",
            performedBy = performedBy
        )
        
        return cancelledPurchase
    }

    @Transactional
    fun deletePurchase(id: Long, deleteVehicle: Boolean = false) {
        val purchase = purchaseRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Compra não encontrada ou já excluída: $id")
        
        // Se deleteVehicle == true, tentar deletar o veículo
        if (deleteVehicle) {
            try {
                vehicleService.deleteVehicle(purchase.vehicle.licensePlate)
            } catch (e: IllegalStateException) {
                // Se o veículo não pode ser deletado (tem outras referências), lança exceção
                throw IllegalStateException("Não é possível deletar o veículo ${purchase.vehicle.licensePlate}: ${e.message}")
            }
        } else {
            // Se não deletar o veículo, cancelar a compra (mudar status para CANCELLED)
            cancelPurchase(id, null)
            return
        }
        
        // Soft delete da compra apenas se deleteVehicle == true
        purchase.deleted = true
        purchaseRepository.save(purchase)
    }

    private fun Purchase.toResponseDTO(): PurchaseResponseDTO {
        return PurchaseResponseDTO(
            id = this.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            vehicleBrand = this.vehicle.brand.name,
            vehicleModel = this.vehicle.modelName,
            partnerCpf = this.partner.cpf,
            partnerName = this.partner.name,
            purchasePrice = this.purchasePrice,
            purchaseDate = this.purchaseDate,
            status = this.status
        )
    }
}
