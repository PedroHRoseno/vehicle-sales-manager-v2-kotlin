package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import java.util.Date

/**
 * DTO unificado para todas as movimentações financeiras
 * Representa Vendas, Compras, Trocas, Custos de Veículos e Transações da Loja
 */
data class FinancialMovementDTO(
    val id: Long,
    val date: Date,
    val description: String,
    val value: Double,
    val type: TransactionTypeEnum, // ENTRY ou EXIT
    val origin: MovementOrigin,    // VEHICLE ou STORE
    val status: TransactionStatus,
    val category: String? = null,  // Categoria (apenas para transações da loja)
    val vehicleLicensePlate: String? = null,  // Placa do veículo (apenas para transações de veículos)
    val transactionType: String? = null  // SALE, PURCHASE, EXCHANGE, COST (apenas para transações de veículos)
)

enum class MovementOrigin {
    VEHICLE,  // Transação relacionada a veículo
    STORE     // Transação geral da loja
}
