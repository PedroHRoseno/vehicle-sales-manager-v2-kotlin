package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository
) {
    fun findByLicensePlate(licensePlate: String): Vehicle? {
        return vehicleRepository.findByLicensePlate(licensePlate)
    }

    fun existsByLicensePlate(licensePlate: String): Boolean {
        return vehicleRepository.existsById(licensePlate)
    }

    fun isAvailable(licensePlate: String): Boolean {
        val vehicle = findByLicensePlate(licensePlate)
        return vehicle?.status == VehicleStatus.DISPONIVEL
    }

    fun getAllVehicles(pageable: Pageable): Page<VehicleResponseDTO> {
        return vehicleRepository.findAll(pageable).map { it.toResponseDTO() }
    }

    fun getAvailableVehicles(pageable: Pageable): Page<VehicleResponseDTO> {
        return vehicleRepository.findByStatus(VehicleStatus.DISPONIVEL, pageable).map { it.toResponseDTO() }
    }

    fun countAvailableVehicles(): Long {
        return vehicleRepository.countByStatus(VehicleStatus.DISPONIVEL)
    }

    @Transactional
    fun createVehicle(dto: VehicleCreateDTO): Vehicle {
        val vehicle = Vehicle(
            licensePlate = dto.licensePlate,
            brand = dto.brand,
            modelName = dto.modelName,
            manufactureYear = dto.manufactureYear,
            modelYear = dto.modelYear,
            color = dto.color,
            kilometersDriven = dto.kilometersDriven,
            status = if (dto.inStock) VehicleStatus.DISPONIVEL else VehicleStatus.VENDIDO
        )
        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun updateVehicleStatus(licensePlate: String, status: VehicleStatus) {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        vehicle.status = status
        vehicleRepository.save(vehicle)
    }

    @Transactional
    fun saveVehicle(vehicle: Vehicle): Vehicle {
        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun deleteVehicle(licensePlate: String) {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        vehicleRepository.delete(vehicle)
    }

    private fun Vehicle.toResponseDTO(): VehicleResponseDTO {
        return VehicleResponseDTO(
            licensePlate = this.licensePlate,
            brand = this.brand,
            modelName = this.modelName,
            manufactureYear = this.manufactureYear,
            modelYear = this.modelYear,
            color = this.color,
            kilometersDriven = this.kilometersDriven,
            status = this.status,
            inStock = this.inStock
        )
    }
}
