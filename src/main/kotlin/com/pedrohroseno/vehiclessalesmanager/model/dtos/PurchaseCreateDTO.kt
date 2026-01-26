package com.pedrohroseno.vehiclessalesmanager.model.dtos

import java.util.Date

data class PurchaseCreateDTO(
    val vehicle: VehicleReferenceDTO,
    val customer: PartnerReferenceDTO,
    val purchasePrice: Double,
    val purchaseDate: String // ISO format string
)
