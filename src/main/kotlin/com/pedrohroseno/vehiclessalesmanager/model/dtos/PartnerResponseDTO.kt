package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class PartnerResponseDTO(
    val cpf: String,
    val name: String,
    val phoneNumber1: String? = null,
    val phoneNumber2: String? = null,
    val address: AddressDTO? = null
)

data class PartnerSummaryDTO(
    val cpf: String,
    val name: String,
    val phoneNumber1: String? = null,
    val city: String? = null
)

data class PartnerDetailDTO(
    val cpf: String,
    val name: String,
    val phoneNumber1: String? = null,
    val phoneNumber2: String? = null,
    val address: AddressDTO? = null,
    val totalSales: Int = 0,
    val totalPurchases: Int = 0,
    val totalExchanges: Int = 0
)
