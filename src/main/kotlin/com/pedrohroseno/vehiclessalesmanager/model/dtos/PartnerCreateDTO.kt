package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class PartnerCreateDTO(
    @JsonProperty("document")
    val document: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("phoneNumber1")
    val phoneNumber1: String? = null,
    @JsonProperty("phoneNumber2")
    val phoneNumber2: String? = null,
    @JsonProperty("address")
    val address: AddressDTO? = null
)

data class AddressDTO(
    @JsonProperty("streetName")
    val streetName: String? = null,
    @JsonProperty("number")
    val number: String? = null,
    @JsonProperty("city")
    val city: String? = null,
    @JsonProperty("state")
    val state: String? = null,
    @JsonProperty("reference")
    val reference: String? = null,
    @JsonProperty("zipCode")
    val zipCode: String? = null
)
