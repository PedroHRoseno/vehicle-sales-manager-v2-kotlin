package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum

data class StoreTransactionCreateDTO(
    val description: String,
    val value: Double,
    val date: String? = null, // ISO format string (yyyy-MM-dd)
    val type: TransactionTypeEnum,
    val category: TransactionCategory
)
