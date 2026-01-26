package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class ExchangeCreateDTO(
    val veiculoEntradaLicensePlate: String,
    val veiculoSaidaLicensePlate: String,
    val valorDiferenca: Double,
    val customerCpf: String? = null // CPF do parceiro (opcional - será buscado via venda anterior se não fornecido)
)
