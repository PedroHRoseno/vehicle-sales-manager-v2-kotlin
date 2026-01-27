package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.TransactionHistory
import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import com.pedrohroseno.vehiclessalesmanager.repository.TransactionHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionHistoryService(
    private val transactionHistoryRepository: TransactionHistoryRepository
) {
    
    @Transactional
    fun logTransaction(
        transactionType: TransactionType,
        transactionId: Long,
        actionType: ActionType,
        description: String? = null,
        oldValue: String? = null,
        newValue: String? = null,
        performedBy: String? = null
    ) {
        val history = TransactionHistory(
            transactionType = transactionType,
            transactionId = transactionId,
            actionType = actionType,
            description = description,
            oldValue = oldValue,
            newValue = newValue,
            performedBy = performedBy
        )
        transactionHistoryRepository.save(history)
    }
}
