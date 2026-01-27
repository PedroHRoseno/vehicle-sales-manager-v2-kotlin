package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.TransactionHistory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionHistoryRepository : JpaRepository<TransactionHistory, Long> {
    fun findByTransactionTypeAndTransactionId(
        transactionType: TransactionType,
        transactionId: Long
    ): List<TransactionHistory>
}
