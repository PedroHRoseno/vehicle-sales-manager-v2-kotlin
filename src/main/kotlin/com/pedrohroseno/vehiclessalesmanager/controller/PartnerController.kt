package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerDetailDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerSummaryDTO
import com.pedrohroseno.vehiclessalesmanager.service.PartnerService
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
@RequestMapping("/partners")
@Tag(name = "Parceiros", description = "API para gerenciamento de parceiros (clientes e fornecedores)")
@CrossOrigin(origins = ["http://localhost:3000"])
class PartnerController(
    private val partnerService: PartnerService
) {
    @GetMapping
    @Operation(summary = "Listar todos os parceiros", description = "Retorna uma lista paginada de todos os parceiros (nome, CPF). Suporta busca por CPF ou nome através do parâmetro 'search'.")
    fun getAllPartners(
        @Parameter(description = "Termo de busca (CPF ou nome)", required = false)
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable
    ): ResponseEntity<Page<PartnerSummaryDTO>> {
        return ResponseEntity.ok(partnerService.getAllPartners(pageable, search))
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Detalhes do parceiro", description = "Retorna detalhes de um parceiro específico, incluindo histórico de operações")
    fun getPartnerDetail(
        @Parameter(description = "CPF do parceiro", required = true)
        @PathVariable cpf: String
    ): ResponseEntity<PartnerDetailDTO> {
        return ResponseEntity.ok(partnerService.getPartnerDetail(cpf))
    }

    @PostMapping
    @Operation(summary = "Criar parceiro", description = "Cria um novo parceiro")
    fun createPartner(@RequestBody dto: PartnerCreateDTO): ResponseEntity<Any> {
        return try {
            partnerService.createOrUpdatePartner(dto)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "Erro desconhecido")))
        }
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualizar parceiro", description = "Atualiza um parceiro existente")
    fun updatePartner(
        @Parameter(description = "CPF do parceiro", required = true)
        @PathVariable cpf: String,
        @RequestBody dto: PartnerCreateDTO
    ): ResponseEntity<Any> {
        return try {
            // Garantir que o CPF do path corresponde ao CPF do DTO
            if (cpf != dto.cpf) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "CPF do path não corresponde ao CPF do corpo da requisição"))
            }
            
            val existingPartner = partnerService.findByCpf(cpf)
            if (existingPartner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Parceiro não encontrado: $cpf"))
            }
            
            partnerService.createOrUpdatePartner(dto)
            ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "Erro desconhecido")))
        }
    }
}
