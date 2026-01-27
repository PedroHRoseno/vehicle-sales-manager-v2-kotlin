package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.StoreTransaction
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.StoreTransactionUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.service.StoreTransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/store-transactions")
@Tag(name = "Transações da Loja", description = "API para gerenciamento de transações financeiras gerais da loja")
@CrossOrigin(origins = ["http://localhost:3000"])
class StoreTransactionController(
    private val storeTransactionService: StoreTransactionService
) {
    @GetMapping
    @Operation(summary = "Listar transações", description = "Retorna uma lista paginada de todas as transações da loja")
    fun getAllTransactions(
        @PageableDefault(size = 20, sort = ["date,desc"]) pageable: Pageable
    ): ResponseEntity<Page<StoreTransactionResponseDTO>> {
        return ResponseEntity.ok(storeTransactionService.getAllTransactions(pageable))
    }

    @PostMapping
    @Operation(summary = "Criar transação", description = "Registra uma nova transação financeira da loja")
    fun createTransaction(@RequestBody dto: StoreTransactionCreateDTO): ResponseEntity<StoreTransactionResponseDTO> {
        val transaction = storeTransactionService.createTransaction(dto)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val response = StoreTransactionResponseDTO(
            id = transaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
            description = transaction.description,
            value = transaction.value,
            date = dateFormat.format(transaction.date),
            type = transaction.type,
            category = transaction.category,
            status = transaction.status
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar transação", description = "Edita uma transação existente")
    fun updateTransaction(
        @Parameter(description = "ID da transação")
        @PathVariable id: Long,
        @RequestBody dto: StoreTransactionUpdateDTO
    ): ResponseEntity<StoreTransactionResponseDTO> {
        return try {
            val transaction = storeTransactionService.updateTransaction(id, dto, null)
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
            val response = StoreTransactionResponseDTO(
                id = transaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
                description = transaction.description,
                value = transaction.value,
                date = dateFormat.format(transaction.date),
                type = transaction.type,
                category = transaction.category,
                status = transaction.status
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar transação", description = "Cancela uma transação (muda status para CANCELLED)")
    fun cancelTransaction(
        @Parameter(description = "ID da transação")
        @PathVariable id: Long
    ): ResponseEntity<StoreTransactionResponseDTO> {
        return try {
            val transaction = storeTransactionService.cancelTransaction(id, null)
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
            val response = StoreTransactionResponseDTO(
                id = transaction.id ?: throw IllegalStateException("StoreTransaction ID não pode ser nulo"),
                description = transaction.description,
                value = transaction.value,
                date = dateFormat.format(transaction.date),
                type = transaction.type,
                category = transaction.category,
                status = transaction.status
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir transação", description = "Cancela uma transação (soft delete)")
    fun deleteTransaction(
        @Parameter(description = "ID da transação")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        storeTransactionService.deleteTransaction(id)
        return ResponseEntity.noContent().build()
    }
}
