package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.VehicleCost
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCostCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCostResponseDTO
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleCostRepository
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.util.Date

@Service
class VehicleCostService(
    private val vehicleCostRepository: VehicleCostRepository,
    @Lazy private val vehicleService: VehicleService,
    private val vehicleRepository: VehicleRepository
) {
    fun getCostsByVehicle(licensePlate: String): List<VehicleCostResponseDTO> {
        return vehicleCostRepository.findByVehicleLicensePlateAndDeletedFalse(licensePlate)
            .map { it.toResponseDTO() }
    }

    fun getTotalCostsByVehicle(licensePlate: String): Double {
        return vehicleCostRepository.sumCostsByVehicleLicensePlate(licensePlate)
    }

    @Transactional
    fun createCost(dto: VehicleCostCreateDTO): VehicleCost {
        val licensePlate = dto.vehicleLicensePlate
            ?: throw IllegalArgumentException("Placa do veículo é obrigatória")
        
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")

        val costDate = if (!dto.costDate.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                dateFormat.parse(dto.costDate)
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

        val cost = VehicleCost(
            vehicle = vehicle,
            cost = dto.cost,
            description = dto.description,
            costDate = costDate
        )

        return vehicleCostRepository.save(cost)
    }

    @Transactional
    fun deleteCost(id: Long) {
        val cost = vehicleCostRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Custo não encontrado: $id") }
        
        cost.deleted = true
        vehicleCostRepository.save(cost)
    }

    private fun VehicleCost.toResponseDTO(): VehicleCostResponseDTO {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return VehicleCostResponseDTO(
            id = this.id ?: throw IllegalStateException("VehicleCost ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            cost = this.cost,
            description = this.description,
            costDate = dateFormat.format(this.costDate)
        )
    }
}
