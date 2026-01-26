package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Purchase
import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCreateDTO
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
    private val vehicleService: VehicleService
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

        return purchaseRepository.save(purchase)
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

        return purchaseRepository.save(purchase)
    }

    @Transactional
    fun deletePurchase(id: Long) {
        val purchase = purchaseRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Compra não encontrada ou já excluída: $id")
        
        // Marcar veículo como excluído (remover do estoque)
        // Como não temos campo deleted no Vehicle, vamos usar um status especial ou deletar fisicamente
        // Por enquanto, vamos deletar fisicamente o veículo
        vehicleService.deleteVehicle(purchase.vehicle.licensePlate)
        
        // Soft delete da compra
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
            purchaseDate = this.purchaseDate
        )
    }
}
