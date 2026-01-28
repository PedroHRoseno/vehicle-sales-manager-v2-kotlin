package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerDetailDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerSummaryDTO
import com.pedrohroseno.vehiclessalesmanager.service.PartnerService
import com.pedrohroseno.vehiclessalesmanager.util.DocumentUtils
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
class PartnerController(
    private val partnerService: PartnerService
) {
    @GetMapping
    @Operation(summary = "Listar todos os parceiros", description = "Retorna uma lista paginada. Suporta busca por documento (CPF/CNPJ) ou nome via parâmetro 'search'.")
    fun getAllPartners(
        @Parameter(description = "Termo de busca (documento ou nome)", required = false)
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable
    ): ResponseEntity<Page<PartnerSummaryDTO>> {
        return ResponseEntity.ok(partnerService.getAllPartners(pageable, search))
    }

    @GetMapping("/{document}")
    @Operation(summary = "Detalhes do parceiro", description = "Retorna detalhes de um parceiro pelo documento (CPF 11 ou CNPJ 14 dígitos).")
    fun getPartnerDetail(
        @Parameter(description = "Documento do parceiro (CPF ou CNPJ)", required = true)
        @PathVariable document: String
    ): ResponseEntity<PartnerDetailDTO> {
        return ResponseEntity.ok(partnerService.getPartnerDetail(document))
    }

    @PostMapping
    @Operation(summary = "Criar parceiro", description = "Cria um novo parceiro. Documento: 11 (CPF) ou 14 (CNPJ) dígitos; não numéricos são removidos.")
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

    @PutMapping("/{document}")
    @Operation(summary = "Atualizar parceiro", description = "Atualiza um parceiro existente. Documento do path e do body (após sanitização) devem coincidir.")
    fun updatePartner(
        @Parameter(description = "Documento do parceiro (CPF ou CNPJ)", required = true)
        @PathVariable document: String,
        @RequestBody dto: PartnerCreateDTO
    ): ResponseEntity<Any> {
        return try {
            val pathDigits = DocumentUtils.sanitize(document)
            val bodyDigits = DocumentUtils.sanitize(dto.document)
            if (pathDigits != bodyDigits) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "Documento do path não corresponde ao documento do corpo da requisição"))
            }
            val existingPartner = partnerService.findByDocument(document)
            if (existingPartner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Parceiro não encontrado: $document"))
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
