package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeResponseDTO
import com.pedrohroseno.vehiclessalesmanager.service.ExchangeService
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
@RequestMapping("/exchanges")
@Tag(name = "Trocas", description = "API para gerenciamento de trocas de veículos")
@CrossOrigin(origins = ["http://localhost:3000"])
class ExchangeController(
    private val exchangeService: ExchangeService
) {
    @GetMapping
    @Operation(summary = "Listar trocas", description = "Retorna uma lista paginada das últimas trocas realizadas")
    fun getAllExchanges(
        @PageableDefault(size = 20, sort = ["exchangeDate"]) pageable: Pageable
    ): ResponseEntity<Page<ExchangeResponseDTO>> {
        return ResponseEntity.ok(exchangeService.getAllExchanges(pageable))
    }

    @PostMapping
    @Operation(
        summary = "Realizar troca",
        description = "Registra uma troca: veículo de saída fica VENDIDO, veículo de entrada fica DISPONIVEL"
    )
    fun createExchange(@RequestBody dto: ExchangeCreateDTO): ResponseEntity<Void> {
        exchangeService.createExchange(dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir troca", description = "Exclui uma troca (soft delete), reverte o status do veículo de saída para DISPONIVEL e remove o veículo de entrada do estoque")
    fun deleteExchange(
        @Parameter(description = "ID da troca a ser excluída")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        exchangeService.deleteExchange(id)
        return ResponseEntity.noContent().build()
    }
}
