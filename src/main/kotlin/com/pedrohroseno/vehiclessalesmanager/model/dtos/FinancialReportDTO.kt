package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class FinancialReportDTO(
    val saldoGeral: Double,
    val totalVendas: Double,
    val totalCompras: Double,
    val totalTrocas: Double,
    val totalCustos: Double,
    val startDate: String,
    val endDate: String
)
