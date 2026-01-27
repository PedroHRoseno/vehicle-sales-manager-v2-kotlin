package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.*
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.ExchangeRepository
import com.pedrohroseno.vehiclessalesmanager.repository.PurchaseRepository
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val saleRepository: SaleRepository,
    private val exchangeRepository: ExchangeRepository,
    private val vehicleCostService: VehicleCostService
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

    fun getVehicleHistory(licensePlate: String): VehicleHistoryDTO {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        
        // Buscar todas as transações relacionadas (incluindo soft-deleted para histórico completo)
        val purchases = purchaseRepository.findAllByVehicleLicensePlateAndDeletedFalse(licensePlate)
            .map { purchase ->
                PurchaseHistoryItem(
                    id = purchase.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
                    purchaseDate = purchase.purchaseDate,
                    purchasePrice = purchase.purchasePrice,
                    partnerCpf = purchase.partner.cpf,
                    partnerName = purchase.partner.name,
                    status = purchase.status
                )
            }
        
        val sales = saleRepository.findAllByVehicleLicensePlateAndDeletedFalse(licensePlate)
            .map { sale ->
                SaleHistoryItem(
                    id = sale.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
                    saleDate = sale.saleDate,
                    salePrice = sale.salePrice,
                    partnerCpf = sale.partner.cpf,
                    partnerName = sale.partner.name,
                    status = sale.status
                )
            }
        
        val exchanges = exchangeRepository.findAllByVehicleLicensePlateAndDeletedFalse(licensePlate)
            .map { exchange ->
                val isIncomingVehicle = exchange.vehicleEntrada.licensePlate == licensePlate
                ExchangeHistoryItem(
                    id = exchange.id ?: throw IllegalStateException("Exchange ID não pode ser nulo"),
                    exchangeDate = exchange.exchangeDate,
                    diferencaValor = exchange.diferencaValor,
                    partnerCpf = exchange.partner.cpf,
                    partnerName = exchange.partner.name,
                    isIncomingVehicle = isIncomingVehicle,
                    status = exchange.status
                )
            }
        
        // Buscar custos adicionais do veículo
        val costs = vehicleCostService.getCostsByVehicle(licensePlate)
        val totalCosts = vehicleCostService.getTotalCostsByVehicle(licensePlate)
        
        return VehicleHistoryDTO(
            vehicle = vehicle.toResponseDTO(),
            purchases = purchases,
            sales = sales,
            exchanges = exchanges,
            costs = costs,
            totalCosts = totalCosts
        )
    }

    @Transactional
    fun deleteVehicle(licensePlate: String) {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        
        // Verificar se há referências em purchases, sales ou exchanges (incluindo soft-deleted)
        val purchaseCount = purchaseRepository.countByVehicleLicensePlate(licensePlate)
        val saleCount = saleRepository.countByVehicleLicensePlate(licensePlate)
        val exchangeCount = exchangeRepository.countByVehicleLicensePlate(licensePlate)
        
        if (purchaseCount > 0 || saleCount > 0 || exchangeCount > 0) {
            throw IllegalStateException(
                "Não é possível deletar o veículo $licensePlate pois ele possui referências em " +
                "${purchaseCount} compra(s), ${saleCount} venda(s) e ${exchangeCount} troca(s)."
            )
        }
        
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
