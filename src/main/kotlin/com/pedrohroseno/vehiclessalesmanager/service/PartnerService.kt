package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Address
import com.pedrohroseno.vehiclessalesmanager.model.Partner
import com.pedrohroseno.vehiclessalesmanager.model.dtos.AddressDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerDetailDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerSummaryDTO
import com.pedrohroseno.vehiclessalesmanager.repository.PartnerRepository
import com.pedrohroseno.vehiclessalesmanager.util.DocumentUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartnerService(
    private val partnerRepository: PartnerRepository
) {
    /** Busca por documento (CPF/CNPJ). Aceita valor já sanitizado ou com pontuação. */
    fun findByDocument(document: String): Partner? {
        val digits = DocumentUtils.sanitize(document)
        if (digits.isEmpty()) return null
        return partnerRepository.findByDocument(digits)
    }

    fun getAllPartners(pageable: Pageable, search: String? = null): Page<PartnerSummaryDTO> {
        return if (search.isNullOrBlank()) {
            partnerRepository.findAllByOrderByNameAsc(pageable).map { it.toSummaryDTO() }
        } else {
            val dig = DocumentUtils.sanitize(search.trim())
            val searchTerm = if (dig.length == 11 || dig.length == 14) dig else search.trim()
            partnerRepository.searchByDocumentOrName(searchTerm, pageable).map { it.toSummaryDTO() }
        }
    }

    fun getPartnerDetail(document: String): PartnerDetailDTO {
        val digits = DocumentUtils.sanitize(document)
        val partner = partnerRepository.findByDocument(digits)
            ?: throw IllegalArgumentException("Parceiro não encontrado: $document")
        return partner.toDetailDTO()
    }

    @Transactional
    fun createOrUpdatePartner(dto: PartnerCreateDTO): Partner {
        val documentDigits = DocumentUtils.requireValid(dto.document)

        val existingPartner = partnerRepository.findByDocument(documentDigits)

        val address = dto.address?.let { addressDto ->
            if (addressDto.city.isNullOrBlank())
                throw IllegalArgumentException("Cidade é obrigatória e não pode estar vazia")
            if (addressDto.number.isNullOrBlank())
                throw IllegalArgumentException("Número é obrigatório e não pode estar vazio")
            if (addressDto.state.isNullOrBlank())
                throw IllegalArgumentException("Estado é obrigatório e não pode estar vazio")
            if (addressDto.zipCode.isNullOrBlank())
                throw IllegalArgumentException("CEP é obrigatório e não pode estar vazio")

            val city = addressDto.city!!.trim()
            val number = addressDto.number!!.trim()
            val state = addressDto.state!!.trim()
            val zipCode = addressDto.zipCode!!.trim()

            if (city.isEmpty()) throw IllegalArgumentException("Cidade não pode ser uma string vazia")
            if (number.isEmpty()) throw IllegalArgumentException("Número não pode ser uma string vazia")
            if (state.isEmpty()) throw IllegalArgumentException("Estado não pode ser uma string vazia")
            if (zipCode.isEmpty()) throw IllegalArgumentException("CEP não pode ser uma string vazia")

            Address().apply {
                streetName = addressDto.streetName?.trim()?.takeIf { it.isNotBlank() }
                this.number = number
                this.city = city
                this.state = state
                reference = addressDto.reference?.trim()?.takeIf { it.isNotBlank() }
                this.zipCode = zipCode
            }
        }

        return if (existingPartner != null) {
            existingPartner.name = dto.name
            existingPartner.phoneNumber1 = dto.phoneNumber1
            existingPartner.phoneNumber2 = dto.phoneNumber2
            existingPartner.address = address
            partnerRepository.save(existingPartner)
        } else {
            val newPartner = Partner(
                document = documentDigits,
                name = dto.name,
                phoneNumber1 = dto.phoneNumber1,
                phoneNumber2 = dto.phoneNumber2,
                address = address
            )
            partnerRepository.save(newPartner)
        }
    }

    private fun Partner.toSummaryDTO(): PartnerSummaryDTO {
        return PartnerSummaryDTO(
            document = this.document,
            name = this.name,
            phoneNumber1 = this.phoneNumber1,
            city = this.address?.city
        )
    }

    private fun Partner.toDetailDTO(): PartnerDetailDTO {
        val addressDTO = this.address?.let {
            AddressDTO(
                streetName = it.streetName,
                number = it.number,
                city = it.city,
                state = it.state,
                reference = it.reference,
                zipCode = it.zipCode
            )
        }
        return PartnerDetailDTO(
            document = this.document,
            name = this.name,
            phoneNumber1 = this.phoneNumber1,
            phoneNumber2 = this.phoneNumber2,
            address = addressDTO,
            totalSales = this.sales.size,
            totalPurchases = this.purchases.size,
            totalExchanges = this.exchanges.size
        )
    }
}
