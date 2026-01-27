package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import java.util.Date

data class PurchaseResponseDTO(
    val id: Long,
    val vehicleLicensePlate: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val partnerCpf: String,
    val partnerName: String,
    val purchasePrice: Double,
    val purchaseDate: Date,
    val status: TransactionStatus
)
