package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus

data class VehicleResponseDTO(
    val licensePlate: String,
    val brand: VehicleBrand,
    val modelName: String,
    val manufactureYear: Int,
    val modelYear: Int,
    val color: String,
    val kilometersDriven: Int,
    val status: VehicleStatus,
    val inStock: Boolean
)
