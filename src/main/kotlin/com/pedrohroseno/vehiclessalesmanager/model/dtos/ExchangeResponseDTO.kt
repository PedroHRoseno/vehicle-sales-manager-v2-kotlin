package com.pedrohroseno.vehiclessalesmanager.model.dtos

import java.util.Date

data class ExchangeResponseDTO(
    val id: Long,
    val vehicleEntradaLicensePlate: String,
    val vehicleEntradaBrand: String,
    val vehicleEntradaModel: String,
    val vehicleSaidaLicensePlate: String,
    val vehicleSaidaBrand: String,
    val vehicleSaidaModel: String,
    val partnerCpf: String,
    val partnerName: String,
    val diferencaValor: Double,
    val exchangeDate: Date
)
