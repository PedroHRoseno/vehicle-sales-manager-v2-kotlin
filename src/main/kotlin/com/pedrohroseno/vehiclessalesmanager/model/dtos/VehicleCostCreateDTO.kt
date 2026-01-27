package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class VehicleCostCreateDTO(
    val vehicleLicensePlate: String? = null, // Vem do path, não precisa estar no body
    val cost: Double,
    val description: String,
    val costDate: String? = null // ISO format string (yyyy-MM-dd)
)
