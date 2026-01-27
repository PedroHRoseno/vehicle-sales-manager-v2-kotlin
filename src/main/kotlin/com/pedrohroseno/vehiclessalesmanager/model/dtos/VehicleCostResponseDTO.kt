package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class VehicleCostResponseDTO(
    val id: Long,
    val vehicleLicensePlate: String,
    val cost: Double,
    val description: String,
    val costDate: String
)
