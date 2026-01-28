package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import java.util.Date

data class VehicleHistoryDTO(
    val vehicle: VehicleResponseDTO,
    val purchases: List<PurchaseHistoryItem>,
    val sales: List<SaleHistoryItem>,
    val exchanges: List<ExchangeHistoryItem>,
    val costs: List<VehicleCostResponseDTO>,
    val totalCosts: Double
)

data class PurchaseHistoryItem(
    val id: Long,
    val purchaseDate: Date,
    val purchasePrice: Double,
    val partnerDocument: String,
    val partnerName: String,
    val status: TransactionStatus
)

data class SaleHistoryItem(
    val id: Long,
    val saleDate: Date,
    val salePrice: Double,
    val partnerDocument: String,
    val partnerName: String,
    val status: TransactionStatus
)

data class ExchangeHistoryItem(
    val id: Long,
    val exchangeDate: Date,
    val diferencaValor: Double,
    val partnerDocument: String,
    val partnerName: String,
    val isIncomingVehicle: Boolean, // true se este veículo é o de entrada, false se é o de saída
    val status: TransactionStatus
)
