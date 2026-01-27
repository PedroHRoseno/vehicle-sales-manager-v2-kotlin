package com.pedrohroseno.vehiclessalesmanager.model.dtos

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum

data class StoreTransactionUpdateDTO(
    val description: String? = null,
    val value: Double? = null,
    val date: String? = null,
    val type: TransactionTypeEnum? = null,
    val category: TransactionCategory? = null
)
