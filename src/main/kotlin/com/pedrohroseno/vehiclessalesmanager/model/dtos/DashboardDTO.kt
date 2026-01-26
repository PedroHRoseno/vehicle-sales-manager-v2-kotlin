package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class DashboardDTO(
    val totalVendas: Double,
    val totalCompras: Double,
    val totalTrocas: Double,
    val saldoLiquido: Double,
    val quantidadeMotosEstoque: Long
)
