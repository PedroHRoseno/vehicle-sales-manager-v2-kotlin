package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Exchange
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.ExchangeUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.ExchangeRepository
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat

@Service
class ExchangeService(
    private val exchangeRepository: ExchangeRepository,
    private val partnerService: PartnerService,
    private val vehicleService: VehicleService,
    private val saleRepository: SaleRepository,
    private val transactionHistoryService: TransactionHistoryService
) {
    fun getAllExchanges(pageable: Pageable): Page<ExchangeResponseDTO> {
        return exchangeRepository.findAllByDeletedFalseOrderByExchangeDateDesc(pageable).map { it.toResponseDTO() }
    }
    @Transactional
    fun createExchange(dto: ExchangeCreateDTO): Exchange {
        // Buscar parceiro - se não fornecido, tenta buscar através de venda anterior do veículo de entrada
        val partner = if (!dto.customerCpf.isNullOrBlank()) {
            partnerService.findByCpf(dto.customerCpf.trim())
                ?: throw IllegalArgumentException("Parceiro não encontrado com CPF: ${dto.customerCpf}. Certifique-se de que o parceiro está cadastrado no sistema.")
        } else {
            // Buscar parceiro através da última venda do veículo de entrada
            val lastSale = saleRepository.findAll()
                .filter { it.vehicle.licensePlate == dto.veiculoEntradaLicensePlate }
                .maxByOrNull { it.saleDate }
            
            lastSale?.partner
                ?: throw IllegalArgumentException("Parceiro não encontrado. O CPF do parceiro é obrigatório quando o veículo de entrada não foi vendido anteriormente pelo sistema. Por favor, selecione o parceiro no formulário.")
        }

        // Buscar veículo de entrada (do cliente)
        val vehicleEntrada = vehicleService.findByLicensePlate(dto.veiculoEntradaLicensePlate)
            ?: throw IllegalArgumentException("Veículo de entrada não encontrado: ${dto.veiculoEntradaLicensePlate}. Certifique-se de que o veículo está cadastrado no sistema.")

        // Buscar veículo de saída (da loja)
        val vehicleSaida = vehicleService.findByLicensePlate(dto.veiculoSaidaLicensePlate)
            ?: throw IllegalArgumentException("Veículo de saída não encontrado: ${dto.veiculoSaidaLicensePlate}")

        // Verificar se o veículo de saída está disponível
        if (!vehicleService.isAvailable(dto.veiculoSaidaLicensePlate)) {
            throw IllegalStateException("Veículo de saída não está disponível: ${dto.veiculoSaidaLicensePlate}")
        }

        // Criar a troca
        val exchange = Exchange(
            vehicleEntrada = vehicleEntrada,
            vehicleSaida = vehicleSaida,
            partner = partner,
            diferencaValor = dto.valorDiferenca,
            exchangeDate = java.util.Date()
        )

        val savedExchange = exchangeRepository.save(exchange)

        // Regras de negócio:
        // 1. Moto de Saída (da loja): Status muda para VENDIDO
        vehicleService.updateVehicleStatus(dto.veiculoSaidaLicensePlate, VehicleStatus.VENDIDO)

        // 2. Moto de Entrada (do cliente): Deve ser cadastrada automaticamente como DISPONIVEL
        // Se o veículo de entrada já existe, apenas atualiza o status
        vehicleEntrada.status = VehicleStatus.DISPONIVEL
        vehicleService.saveVehicle(vehicleEntrada)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.EXCHANGE,
            transactionId = savedExchange.id ?: throw IllegalStateException("Exchange ID não pode ser nulo"),
            actionType = ActionType.CREATED,
            description = "Troca criada: Diferença R$ ${dto.valorDiferenca} - Entrada: ${dto.veiculoEntradaLicensePlate}, Saída: ${dto.veiculoSaidaLicensePlate}",
            performedBy = null
        )

        return savedExchange
    }

    @Transactional
    fun updateExchange(id: Long, dto: ExchangeUpdateDTO, performedBy: String? = null): Exchange {
        val exchange = exchangeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Troca não encontrada ou já excluída: $id")
        
        if (exchange.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Não é possível editar uma troca cancelada")
        }
        
        val oldDiferenca = exchange.diferencaValor
        val oldDate = exchange.exchangeDate
        
        // Atualizar diferença de valor se fornecida
        if (dto.diferencaValor != null) {
            exchange.diferencaValor = dto.diferencaValor
        }
        
        // Atualizar data se fornecida
        if (!dto.exchangeDate.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                exchange.exchangeDate = dateFormat.parse(dto.exchangeDate)
            } catch (e: Exception) {
                throw IllegalArgumentException("Data inválida: ${dto.exchangeDate}")
            }
        }
        
        val updatedExchange = exchangeRepository.save(exchange)
        
        // Log histórico
        val description = buildString {
            if (dto.diferencaValor != null && dto.diferencaValor != oldDiferenca) {
                append("Diferença de valor alterada de R$ $oldDiferenca para R$ ${dto.diferencaValor}. ")
            }
            if (!dto.exchangeDate.isNullOrBlank()) {
                append("Data atualizada. ")
            }
            append("Saldo atualizado.")
        }
        
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.EXCHANGE,
            transactionId = updatedExchange.id ?: throw IllegalStateException("Exchange ID não pode ser nulo"),
            actionType = ActionType.EDITED,
            description = "Troca editada: $description",
            oldValue = "Diferença: R$ $oldDiferenca, Data: $oldDate",
            newValue = "Diferença: R$ ${updatedExchange.diferencaValor}, Data: ${updatedExchange.exchangeDate}",
            performedBy = performedBy
        )
        
        return updatedExchange
    }

    @Transactional
    fun cancelExchange(id: Long, performedBy: String? = null): Exchange {
        val exchange = exchangeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Troca não encontrada ou já excluída: $id")
        
        if (exchange.status == TransactionStatus.CANCELLED) {
            throw IllegalStateException("Troca já está cancelada")
        }
        
        exchange.status = TransactionStatus.CANCELLED
        
        // Reverter status do veículo de saída para DISPONIVEL
        vehicleService.updateVehicleStatus(exchange.vehicleSaida.licensePlate, VehicleStatus.DISPONIVEL)
        
        val cancelledExchange = exchangeRepository.save(exchange)
        
        // Log histórico
        transactionHistoryService.logTransaction(
            transactionType = TransactionType.EXCHANGE,
            transactionId = cancelledExchange.id ?: throw IllegalStateException("Exchange ID não pode ser nulo"),
            actionType = ActionType.CANCELLED,
            description = "Troca cancelada: Diferença R$ ${exchange.diferencaValor} - Entrada: ${exchange.vehicleEntrada.licensePlate}, Saída: ${exchange.vehicleSaida.licensePlate}. Status do veículo de saída revertido para DISPONIVEL.",
            performedBy = performedBy
        )
        
        return cancelledExchange
    }

    @Transactional
    fun deleteExchange(id: Long, deleteIncomingVehicle: Boolean = false) {
        val exchange = exchangeRepository.findByIdAndDeletedFalse(id)
            ?: throw IllegalArgumentException("Troca não encontrada ou já excluída: $id")
        
        // Reverter status do veículo de saída para DISPONIVEL (sempre)
        vehicleService.updateVehicleStatus(exchange.vehicleSaida.licensePlate, VehicleStatus.DISPONIVEL)
        
        // Se deleteIncomingVehicle == true, tentar deletar o veículo de entrada
        if (deleteIncomingVehicle) {
            try {
                vehicleService.deleteVehicle(exchange.vehicleEntrada.licensePlate)
            } catch (e: IllegalStateException) {
                // Se o veículo não pode ser deletado (tem outras referências), lança exceção
                throw IllegalStateException("Não é possível deletar o veículo de entrada ${exchange.vehicleEntrada.licensePlate}: ${e.message}")
            }
        }
        
        // Soft delete da troca
        exchange.deleted = true
        exchangeRepository.save(exchange)
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
            partnerCpf = this.partner.cpf,
            partnerName = this.partner.name,
            diferencaValor = this.diferencaValor,
            exchangeDate = this.exchangeDate,
            status = this.status
        )
    }
}
