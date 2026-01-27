package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum

data class StoreTransactionResponseDTO(
    val id: Long,
    val description: String,
    val value: Double,
    val date: String,
    val type: TransactionTypeEnum,
    val category: TransactionCategory,
    val status: TransactionStatus
)
