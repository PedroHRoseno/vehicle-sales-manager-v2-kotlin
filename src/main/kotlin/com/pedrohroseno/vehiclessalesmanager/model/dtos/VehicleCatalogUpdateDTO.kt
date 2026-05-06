package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class VehicleCatalogUpdateDTO(
    val published: Boolean? = null,
    val imageUrlList: List<String>? = null,
    val description: String? = null
)

