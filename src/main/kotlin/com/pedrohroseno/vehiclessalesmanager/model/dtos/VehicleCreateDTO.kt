package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand

data class VehicleCreateDTO(
    val licensePlate: String,
    val brand: VehicleBrand,
    val modelName: String,
    val manufactureYear: Int,
    val modelYear: Int,
    val color: String,
    val kilometersDriven: Int,
    val inStock: Boolean = true
)
