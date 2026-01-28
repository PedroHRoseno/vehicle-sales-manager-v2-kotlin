package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class SaleCreateDTO(
    val vehicle: VehicleReferenceDTO,
    val customer: PartnerReferenceDTO,
    val salePrice: Double
)

data class VehicleReferenceDTO(
    val licensePlate: String
)

data class PartnerReferenceDTO(
    val document: String
)
