package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Sale
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SaleService(
    private val saleRepository: SaleRepository,
    private val partnerService: PartnerService,
    private val vehicleService: VehicleService
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
        val partner = partnerService.findByCpf(dto.customer.cpf)
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

        return savedSale
    }

    @Transactional
    fun deleteSale(id: Long) {
        val sale = saleRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Venda não encontrada ou já excluída: $id")
        
        // Reverter status do veículo para DISPONIVEL
        vehicleService.updateVehicleStatus(sale.vehicle.licensePlate, VehicleStatus.DISPONIVEL)
        
        // Soft delete
        sale.deleted = true
        saleRepository.save(sale)
    }

    private fun Sale.toResponseDTO(): SaleResponseDTO {
        return SaleResponseDTO(
            id = this.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            vehicleBrand = this.vehicle.brand.name,
            vehicleModel = this.vehicle.modelName,
            partnerCpf = this.partner.cpf,
            partnerName = this.partner.name,
            salePrice = this.salePrice,
            saleDate = this.saleDate
        )
    }
}
