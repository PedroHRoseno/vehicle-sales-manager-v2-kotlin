package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import org.springframework.stereotype.Component

@Component
class PurchaseStockEffect(
    private val vehicleService: VehicleService
) : StockEffectStrategy {

    override fun applyOnCreate(licensePlate: String) {
        vehicleService.updateVehicleStatus(licensePlate, VehicleStatus.DISPONIVEL)
    }

    override fun applyOnCancel(licensePlate: String) {
        vehicleService.updateVehicleStatus(licensePlate, VehicleStatus.INACTIVE)
    }
}
