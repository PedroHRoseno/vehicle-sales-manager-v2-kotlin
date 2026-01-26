package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerCreateDTO
import com.pedrohroseno.vehiclessalesmanager.service.ExchangeService
import com.pedrohroseno.vehiclessalesmanager.service.PartnerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller de compatibilidade para manter endpoints antigos funcionando
 * enquanto o front-end é atualizado para usar os novos endpoints em inglês
 */
@RestController
@Tag(name = "Compatibilidade", description = "Endpoints de compatibilidade com versões antigas do front-end")
@CrossOrigin(origins = ["http://localhost:3000"])
class CompatibilityController(
    private val partnerService: PartnerService,
    private val exchangeService: ExchangeService
) {
    @PostMapping("/customers")
    @Operation(summary = "Criar ou atualizar parceiro (compatibilidade)", description = "Alias para /partners")
    fun createOrUpdatePartnerCompat(@RequestBody dto: PartnerCreateDTO): ResponseEntity<Any> {
        return try {
            partnerService.createOrUpdatePartner(dto)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "Erro desconhecido")))
        }
    }

    @PostMapping("/trocas")
    @Operation(summary = "Realizar troca (compatibilidade)", description = "Alias para /exchanges")
    fun createExchangeCompat(@RequestBody dto: ExchangeCreateDTO): ResponseEntity<Void> {
        exchangeService.createExchange(dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
