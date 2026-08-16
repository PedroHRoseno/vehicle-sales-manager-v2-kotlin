package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialMovementDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Date

interface FinancialMovementQueryRepository {
    fun findMovements(
        pageable: Pageable,
        startDate: Date? = null,
        endDate: Date? = null,
        type: TransactionTypeEnum? = null,
        category: String? = null
    ): Page<FinancialMovementDTO>
}
