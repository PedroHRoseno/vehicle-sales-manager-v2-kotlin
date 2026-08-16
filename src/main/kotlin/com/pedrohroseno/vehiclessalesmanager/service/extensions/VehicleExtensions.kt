package com.pedrohroseno.vehiclessalesmanager.service.extensions

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PublicVehicleDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleResponseDTO

fun Vehicle.toResponseDTO(): VehicleResponseDTO {
    return VehicleResponseDTO(
        licensePlate = this.licensePlate,
        brand = this.brand,
        modelName = this.modelName,
        manufactureYear = this.manufactureYear,
        modelYear = this.modelYear,
        color = this.color,
        kilometersDriven = this.kilometersDriven,
        status = this.status,
        inStock = this.inStock,
        published = this.published,
        description = this.description,
        imageUrlList = this.imageUrlList.toList()
    )
}

fun Vehicle.toPublicDTO(): PublicVehicleDTO {
    return PublicVehicleDTO(
        brand = this.brand.name,
        model = this.modelName,
        year = this.modelYear,
        color = this.color,
        kilometersDriven = this.kilometersDriven,
        imageUrlList = this.imageUrlList.toList(),
        description = this.description
    )
}