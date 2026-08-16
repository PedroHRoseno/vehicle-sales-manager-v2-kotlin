package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.dtos.*
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.ExchangeRepository
import com.pedrohroseno.vehiclessalesmanager.repository.PurchaseRepository
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import com.pedrohroseno.vehiclessalesmanager.service.extensions.toPublicDTO
import com.pedrohroseno.vehiclessalesmanager.service.extensions.toResponseDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val saleRepository: SaleRepository,
    private val exchangeRepository: ExchangeRepository,
) {
    fun findByLicensePlate(licensePlate: String): Vehicle? {
        return vehicleRepository.findByLicensePlate(licensePlate)
    }

    fun existsByLicensePlate(licensePlate: String): Boolean {
        return vehicleRepository.existsById(licensePlate)
    }

    fun isAvailable(licensePlate: String): Boolean {
        val vehicle = findByLicensePlate(licensePlate)
        return vehicle?.status == VehicleStatus.DISPONIVEL
    }

    fun getAllVehicles(
        pageable: Pageable,
        search: String? = null,
        inStock: Boolean? = null,
        published: Boolean? = null
    ): Page<VehicleResponseDTO> {
        val searchTerm = search?.trim()?.takeIf { it.isNotEmpty() }
        val status = when (inStock) {
            true -> VehicleStatus.DISPONIVEL
            false -> VehicleStatus.VENDIDO
            null -> null
        }
        val effectivePageable = resolveVehiclePageable(pageable)
        return vehicleRepository
            .findFiltered(searchTerm, status, published, effectivePageable)
            .map { it.toResponseDTO() }
    }

    /** Garante ordenação por createdAt (mais recentes primeiro), com fallback seguro. */
    private fun resolveVehiclePageable(pageable: Pageable): Pageable {
        val sort = pageable.sort
        if (sort.isSorted) {
            return pageable
        }
        return PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
    }

    fun getAvailableVehicles(pageable: Pageable): Page<VehicleResponseDTO> {
        return vehicleRepository.findByStatus(VehicleStatus.DISPONIVEL, pageable).map { it.toResponseDTO() }
    }

    fun getPublicAvailableVehicles(
        brand: String? = null,
        maxKm: Int? = null,
        yearMin: Int? = null,
    ): List<PublicVehicleDTO> {
        val parsedBrand = parsePublicBrand(brand)
        if (!brand.isNullOrBlank() && parsedBrand == null) {
            return emptyList()
        }
        return vehicleRepository
            .findPublicCatalog(VehicleStatus.DISPONIVEL, parsedBrand, maxKm, yearMin)
            .map { it.toPublicDTO() }
    }

    private fun parsePublicBrand(raw: String?): VehicleBrand? {
        if (raw.isNullOrBlank()) return null
        val normalized = raw.trim().uppercase().replace(' ', '_').replace('-', '_')
        return VehicleBrand.entries.find { it.name.equals(normalized, ignoreCase = true) }
            ?: VehicleBrand.entries.find {
                it.name.replace("_", "").equals(normalized.replace("_", ""), ignoreCase = true)
            }
    }

    fun countAvailableVehicles(): Long {
        return vehicleRepository.countByStatus(VehicleStatus.DISPONIVEL)
    }

    @Transactional
    fun createVehicle(dto: VehicleCreateDTO): Vehicle {
        val vehicle = Vehicle(
            licensePlate = dto.licensePlate,
            brand = dto.brand,
            modelName = dto.modelName,
            manufactureYear = dto.manufactureYear,
            modelYear = dto.modelYear,
            color = dto.color,
            kilometersDriven = dto.kilometersDriven,
            published = dto.published,
            description = dto.description?.trim()?.takeIf { it.isNotBlank() },
            imageUrlList = dto.imageUrlList.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }.toMutableList(),
            status = if (dto.inStock) VehicleStatus.DISPONIVEL else VehicleStatus.VENDIDO
        )
        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun updateVehicle(dto: VehicleCreateDTO): Vehicle {
        val vehicle = vehicleRepository.findByLicensePlate(dto.licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: ${dto.licensePlate}")

        vehicle.brand = dto.brand
        vehicle.modelName = dto.modelName
        vehicle.manufactureYear = dto.manufactureYear
        vehicle.modelYear = dto.modelYear
        vehicle.color = dto.color
        vehicle.kilometersDriven = dto.kilometersDriven
        vehicle.published = dto.published
        vehicle.description = dto.description?.trim()?.takeIf { it.isNotBlank() }
        vehicle.imageUrlList = dto.imageUrlList.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }.toMutableList()
        vehicle.status = if (dto.inStock) VehicleStatus.DISPONIVEL else VehicleStatus.VENDIDO
        if (vehicle.status != VehicleStatus.DISPONIVEL && vehicle.published) {
            vehicle.published = false
        }

        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun updateVehicleStatus(licensePlate: String, status: VehicleStatus) {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        vehicle.status = status
        if (status != VehicleStatus.DISPONIVEL && vehicle.published) {
            vehicle.published = false
        }
        vehicleRepository.save(vehicle)
    }

    @Transactional
    fun updateCatalog(licensePlate: String, dto: VehicleCatalogUpdateDTO): Vehicle {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")

        if (dto.published != null) {
            vehicle.published = dto.published
        }
        if (dto.description != null) {
            vehicle.description = dto.description.trim().takeIf { it.isNotBlank() }
        }
        if (dto.imageUrlList != null) {
            vehicle.imageUrlList = dto.imageUrlList
                .mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
                .toMutableList()
        }

        if (vehicle.status != VehicleStatus.DISPONIVEL && vehicle.published) {
            vehicle.published = false
        }

        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun saveVehicle(vehicle: Vehicle): Vehicle {
        return vehicleRepository.save(vehicle)
    }

    @Transactional
    fun deleteVehicle(licensePlate: String) {
        val vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            ?: throw IllegalArgumentException("Veículo não encontrado: $licensePlate")
        
        // Verificar se há referências em purchases, sales ou exchanges (incluindo soft-deleted)
        val purchaseCount = purchaseRepository.countByVehicleLicensePlate(licensePlate)
        val saleCount = saleRepository.countByVehicleLicensePlate(licensePlate)
        val exchangeCount = exchangeRepository.countByVehicleLicensePlate(licensePlate)
        
        if (purchaseCount > 0 || saleCount > 0 || exchangeCount > 0) {
            throw IllegalStateException(
                "Não é possível deletar o veículo $licensePlate pois ele possui referências em " +
                "${purchaseCount} compra(s), ${saleCount} venda(s) e ${exchangeCount} troca(s)."
            )
        }
        
        vehicleRepository.delete(vehicle)
    }
}
