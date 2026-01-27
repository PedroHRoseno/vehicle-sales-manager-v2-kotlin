package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class DashboardDTO(
    val totalVendas: Double,
    val totalCompras: Double,
    val totalTrocas: Double,
    val totalCustos: Double,
    val despesasOperacionais: Double,
    val lucroBruto: Double,
    val lucroLiquido: Double,
    val saldoLiquido: Double, // Mantido para compatibilidade (igual a lucroLiquido)
    val quantidadeMotosEstoque: Long
)
