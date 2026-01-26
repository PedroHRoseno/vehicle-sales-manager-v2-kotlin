package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Address
import com.pedrohroseno.vehiclessalesmanager.model.Partner
import com.pedrohroseno.vehiclessalesmanager.model.dtos.AddressDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerDetailDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PartnerSummaryDTO
import com.pedrohroseno.vehiclessalesmanager.repository.PartnerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartnerService(
    private val partnerRepository: PartnerRepository
) {
    fun findByCpf(cpf: String): Partner? {
        return partnerRepository.findByCpf(cpf)
    }

    fun getAllPartners(pageable: Pageable, search: String? = null): Page<PartnerSummaryDTO> {
        return if (search.isNullOrBlank()) {
            partnerRepository.findAllByOrderByNameAsc(pageable).map { it.toSummaryDTO() }
        } else {
            partnerRepository.searchByCpfOrName(search.trim(), pageable).map { it.toSummaryDTO() }
        }
    }

    fun getPartnerDetail(cpf: String): PartnerDetailDTO {
        val partner = partnerRepository.findByCpf(cpf)
            ?: throw IllegalArgumentException("Parceiro não encontrado: $cpf")
        return partner.toDetailDTO()
    }

    @Transactional
    fun createOrUpdatePartner(dto: PartnerCreateDTO): Partner {
        // Debug: log do DTO recebido
        println("DEBUG - DTO recebido: cpf=${dto.cpf}, name=${dto.name}")
        println("DEBUG - Address: ${dto.address}")
        if (dto.address != null) {
            println("DEBUG - Address.city: '${dto.address.city}'")
            println("DEBUG - Address.number: '${dto.address.number}'")
            println("DEBUG - Address.state: '${dto.address.state}'")
            println("DEBUG - Address.zipCode: '${dto.address.zipCode}'")
        }
        
        val existingPartner = partnerRepository.findByCpf(dto.cpf)
        
        val address = dto.address?.let { addressDto ->
            println("DEBUG Service - addressDto recebido: $addressDto")
            println("DEBUG Service - addressDto.city: '${addressDto.city}' (isNullOrBlank: ${addressDto.city.isNullOrBlank()})")
            println("DEBUG Service - addressDto.number: '${addressDto.number}' (isNullOrBlank: ${addressDto.number.isNullOrBlank()})")
            println("DEBUG Service - addressDto.state: '${addressDto.state}' (isNullOrBlank: ${addressDto.state.isNullOrBlank()})")
            println("DEBUG Service - addressDto.zipCode: '${addressDto.zipCode}' (isNullOrBlank: ${addressDto.zipCode.isNullOrBlank()})")
            
            // Validar campos obrigatórios ANTES de criar o Address
            if (addressDto.city.isNullOrBlank()) {
                println("DEBUG - ERRO: Cidade está null ou vazia")
                throw IllegalArgumentException("Cidade é obrigatória e não pode estar vazia")
            }
            if (addressDto.number.isNullOrBlank()) {
                println("DEBUG - ERRO: Número está null ou vazio")
                throw IllegalArgumentException("Número é obrigatório e não pode estar vazio")
            }
            if (addressDto.state.isNullOrBlank()) {
                println("DEBUG - ERRO: Estado está null ou vazio")
                throw IllegalArgumentException("Estado é obrigatório e não pode estar vazio")
            }
            if (addressDto.zipCode.isNullOrBlank()) {
                println("DEBUG - ERRO: CEP está null ou vazio")
                throw IllegalArgumentException("CEP é obrigatório e não pode estar vazio")
            }
            
            // Garantir que os valores não são null após validação
            val city = addressDto.city!!.trim()
            val number = addressDto.number!!.trim()
            val state = addressDto.state!!.trim()
            val zipCode = addressDto.zipCode!!.trim()
            
            println("DEBUG Service - Valores após trim: city='$city', number='$number', state='$state', zipCode='$zipCode'")
            
            // Validação adicional: garantir que não são strings vazias após trim
            if (city.isEmpty()) {
                throw IllegalArgumentException("Cidade não pode ser uma string vazia")
            }
            if (number.isEmpty()) {
                throw IllegalArgumentException("Número não pode ser uma string vazia")
            }
            if (state.isEmpty()) {
                throw IllegalArgumentException("Estado não pode ser uma string vazia")
            }
            if (zipCode.isEmpty()) {
                throw IllegalArgumentException("CEP não pode ser uma string vazia")
            }
            
            val createdAddress = Address().apply {
                this.streetName = addressDto.streetName?.trim()?.takeIf { street -> street.isNotBlank() }
                this.number = number
                this.city = city
                this.state = state
                this.reference = addressDto.reference?.trim()?.takeIf { ref -> ref.isNotBlank() }
                this.zipCode = zipCode
            }
            
            println("DEBUG Service - Address criado: city='${createdAddress.city}', number='${createdAddress.number}', state='${createdAddress.state}', zipCode='${createdAddress.zipCode}'")
            
            createdAddress
        }

        // Debug: verificar o address antes de salvar
        println("DEBUG Service - Address antes de salvar: $address")
        if (address != null) {
            println("DEBUG Service - Address.city: '${address.city}'")
            println("DEBUG Service - Address.number: '${address.number}'")
            println("DEBUG Service - Address.state: '${address.state}'")
            println("DEBUG Service - Address.zipCode: '${address.zipCode}'")
        }
        
        return if (existingPartner != null) {
            existingPartner.name = dto.name
            existingPartner.phoneNumber1 = dto.phoneNumber1
            existingPartner.phoneNumber2 = dto.phoneNumber2
            existingPartner.address = address
            println("DEBUG Service - Salvando parceiro existente com address: ${existingPartner.address}")
            partnerRepository.save(existingPartner)
        } else {
            val newPartner = Partner(
                cpf = dto.cpf,
                name = dto.name,
                phoneNumber1 = dto.phoneNumber1,
                phoneNumber2 = dto.phoneNumber2,
                address = address
            )
            println("DEBUG Service - Criando novo parceiro com address: ${newPartner.address}")
            partnerRepository.save(newPartner)
        }
    }

    private fun Partner.toSummaryDTO(): PartnerSummaryDTO {
        return PartnerSummaryDTO(
            cpf = this.cpf,
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
            cpf = this.cpf,
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
