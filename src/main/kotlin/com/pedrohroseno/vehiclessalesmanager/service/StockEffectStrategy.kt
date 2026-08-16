package com.pedrohroseno.vehiclessalesmanager.service

/**
 * Encapsula o efeito de uma operação (venda, compra, etc.) sobre o estoque.
 * Cada implementação decide QUAIS status aplicar; o VehicleService executa a mudança.
 */
interface StockEffectStrategy {
    fun applyOnCreate(licensePlate: String)
    fun applyOnCancel(licensePlate: String)
}
