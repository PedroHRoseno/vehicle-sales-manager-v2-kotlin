package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class ExchangeCreateDTO(
    val veiculoEntradaLicensePlate: String,
    val veiculoSaidaLicensePlate: String,
    val valorDiferenca: Double,
    val customerDocument: String? = null // Documento do parceiro – CPF 11 ou CNPJ 14 dígitos (opcional – será buscado via venda anterior se não fornecido)
)
