package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.StoreTransaction
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import com.pedrohroseno.vehiclessalesmanager.repository.StoreTransactionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.util.Date

@Service
class StoreTransactionService(
    private val storeTransactionRepository: StoreTransactionRepository,
    private val transactionHistoryService: TransactionHistoryService
) {
    fun getAllTransactions(pageable: Pageable): Page<StoreTransactionResponseDTO> {
        return storeTransactionRepository.findAllByDeletedFalseOrderByDateDesc(pageable)
            .map { it.toResponseDTO() }
    }

    @Transactional
    fun createTransaction(dto: StoreTransactionCreateDTO): StoreTransaction {
        val date = if (!dto.date.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                dateFormat.parse(dto.date)
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

        val transaction = StoreTransaction(
            description = dto.description,
            value = dto.value,
            date = date,
            type = dto.type,
            category = dto.category
        )

        val savedTransaction = storeTransactionRepository.save(transaction)

        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.STORE_TRANSACTION,
            transactionId = savedTransaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
            actionType = ActionType.CREATED,
            description = "Transação da loja criada: ${dto.type.name} - R$ ${dto.value} - ${dto.description}",
            performedBy = null
        )

        return savedTransaction
    }

    @Transactional
    fun updateTransaction(id: Long, dto: StoreTransactionUpdateDTO, performedBy: String? = null): StoreTransaction {
        val transaction = storeTransactionRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Transação não encontrada ou já excluída: $id")

        if (transaction.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Não é possível editar uma transação cancelada")
        }

        val oldDescription = transaction.description
        val oldValue = transaction.value
        val oldDate = transaction.date
        val oldType = transaction.type
        val oldCategory = transaction.category

        // Atualizar campos se fornecidos
        if (dto.description != null) {
            transaction.description = dto.description
        }
        if (dto.value != null && dto.value > 0) {
            transaction.value = dto.value
        }
        if (!dto.date.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                transaction.date = dateFormat.parse(dto.date)
            } catch (e: Exception) {
                throw IllegalArgumentException("Data inválida: ${dto.date}")
            }
        }
        if (dto.type != null) {
            transaction.type = dto.type
        }
        if (dto.category != null) {
            transaction.category = dto.category
        }

        val updatedTransaction = storeTransactionRepository.save(transaction)

        // Log histórico
        val description = buildString {
            if (dto.description != null && dto.description != oldDescription) {
                append("Descrição alterada. ")
            }
            if (dto.value != null && dto.value != oldValue) {
                append("Valor alterado de R$ $oldValue para R$ ${dto.value}. ")
            }
            if (!dto.date.isNullOrBlank()) {
                append("Data atualizada. ")
            }
            if (dto.type != null && dto.type != oldType) {
                append("Tipo alterado de ${oldType.name} para ${dto.type.name}. ")
            }
            if (dto.category != null && dto.category != oldCategory) {
                append("Categoria alterada de ${oldCategory.name} para ${dto.category.name}. ")
            }
        }

        transactionHistoryService.logTransaction(
            transactionType = TransactionType.STORE_TRANSACTION,
            transactionId = updatedTransaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
            actionType = ActionType.EDITED,
            description = "Transação da loja editada: $description",
            oldValue = "Descrição: $oldDescription, Valor: R$ $oldValue, Data: $oldDate, Tipo: ${oldType.name}, Categoria: ${oldCategory.name}",
            newValue = "Descrição: ${updatedTransaction.description}, Valor: R$ ${updatedTransaction.value}, Data: ${updatedTransaction.date}, Tipo: ${updatedTransaction.type.name}, Categoria: ${updatedTransaction.category.name}",
            performedBy = performedBy
        )

        return updatedTransaction
    }

    @Transactional
    fun cancelTransaction(id: Long, performedBy: String? = null): StoreTransaction {
        val transaction = storeTransactionRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Transação não encontrada ou já excluída: $id")

        if (transaction.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Transação já está cancelada")
        }

        transaction.status = TransactionStatus.CANCELLED
        val cancelledTransaction = storeTransactionRepository.save(transaction)

        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.STORE_TRANSACTION,
            transactionId = cancelledTransaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
            actionType = ActionType.CANCELLED,
            description = "Transação da loja cancelada: ${transaction.type.name} - R$ ${transaction.value} - ${transaction.description}",
            performedBy = performedBy
        )

        return cancelledTransaction
    }

    @Transactional
    fun deleteTransaction(id: Long) {
        // deleteTransaction agora chama cancelTransaction para manter consistência
        cancelTransaction(id, null)
    }

    private fun StoreTransaction.toResponseDTO(): StoreTransactionResponseDTO {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return StoreTransactionResponseDTO(
            id = this.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
            description = this.description,
            value = this.value,
            date = dateFormat.format(this.date),
            type = this.type,
            category = this.category,
            status = this.status
        )
    }
}
