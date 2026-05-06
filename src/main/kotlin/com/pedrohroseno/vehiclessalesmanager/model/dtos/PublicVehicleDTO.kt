package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class PublicVehicleDTO(
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val kilometersDriven: Int,
    val imageUrlList: List<String>,
    val description: String? = null
)

