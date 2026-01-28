package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.Exchange
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeUpdateDTO
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

    @PutMapping("/{id}")
    @Operation(
        summary = "Editar troca",
        description = "Edita uma troca existente. Permite alterar apenas diferença de valor e data. Não permite alterar veículos ou parceiro."
    )
    fun updateExchange(
        @Parameter(description = "ID da troca a ser editada")
        @PathVariable id: Long,
        @RequestBody dto: ExchangeUpdateDTO
    ): ResponseEntity<ExchangeResponseDTO> {
        return try {
            val exchange = exchangeService.updateExchange(id, dto, null)
            ResponseEntity.ok(exchange.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar troca",
        description = "Cancela uma troca. Transações canceladas não aparecem nos cálculos financeiros. O veículo de saída é revertido para DISPONIVEL."
    )
    fun cancelExchange(
        @Parameter(description = "ID da troca a ser cancelada")
        @PathVariable id: Long
    ): ResponseEntity<ExchangeResponseDTO> {
        return try {
            val exchange = exchangeService.cancelExchange(id, null)
            ResponseEntity.ok(exchange.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir troca",
        description = "Exclui uma troca (soft delete) e reverte o status do veículo de saída para DISPONIVEL. Se deleteIncomingVehicle=true, também remove o veículo de entrada do estoque."
    )
    fun deleteExchange(
        @Parameter(description = "ID da troca a ser excluída")
        @PathVariable id: Long,
        @Parameter(description = "Se true, também deleta o veículo de entrada. Se false, apenas exclui o registro da troca.")
        @RequestParam(required = false, defaultValue = "false") deleteIncomingVehicle: Boolean
    ): ResponseEntity<Void> {
        return try {
            exchangeService.deleteExchange(id, deleteIncomingVehicle)
            ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
    
    private fun Exchange.toResponseDTO(): ExchangeResponseDTO {
        return ExchangeResponseDTO(
            id = this.id ?: throw IllegalStateException("Exchange ID não pode ser nulo"),
            vehicleEntradaLicensePlate = this.vehicleEntrada.licensePlate,
            vehicleEntradaBrand = this.vehicleEntrada.brand.name,
            vehicleEntradaModel = this.vehicleEntrada.modelName,
            vehicleSaidaLicensePlate = this.vehicleSaida.licensePlate,
            vehicleSaidaBrand = this.vehicleSaida.brand.name,
            vehicleSaidaModel = this.vehicleSaida.modelName,
            partnerDocument = this.partner.document,
            partnerName = this.partner.name,
            diferencaValor = this.diferencaValor,
            exchangeDate = this.exchangeDate,
            status = this.status
        )
    }
}
