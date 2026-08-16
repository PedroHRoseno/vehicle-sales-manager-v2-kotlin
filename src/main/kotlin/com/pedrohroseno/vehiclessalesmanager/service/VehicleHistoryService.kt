package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.dtos.*
import com.pedrohroseno.vehiclessalesmanager.repository.ExchangeRepository
import com.pedrohroseno.vehiclessalesmanager.repository.PurchaseRepository
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import com.pedrohroseno.vehiclessalesmanager.service.extensions.toResponseDTO
import org.springframework.stereotype.Service

@Service
class VehicleHistoryService(
    private val vehicleRepository: VehicleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val saleRepository: SaleRepository,
    private val exchangeRepository: ExchangeRepository,
    private val vehicleCostService: VehicleCostService
){

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
                    partnerDocument = purchase.partner.document,
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
                    partnerDocument = sale.partner.document,
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
                    partnerDocument = exchange.partner.document,
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

}