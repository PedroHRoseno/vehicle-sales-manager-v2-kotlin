package com.pedrohroseno.vehiclessalesmanager.model.dtos

import java.util.Date

data class SaleResponseDTO(
    val id: Long,
    val vehicleLicensePlate: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val partnerCpf: String,
    val partnerName: String,
    val salePrice: Double,
    val saleDate: Date
)
