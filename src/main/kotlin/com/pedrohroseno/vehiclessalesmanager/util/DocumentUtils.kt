package com.pedrohroseno.vehiclessalesmanager.util

/**
 * Utilitário para documento de identificação (CPF ou CNPJ).
 * Aceita 11 dígitos (CPF) ou 14 dígitos (CNPJ). Sanitiza removendo caracteres não numéricos.
 */
object DocumentUtils {

    private val ONLY_DIGITS = Regex("[^0-9]")

    /** Remove pontos, traços, barras etc., retornando apenas dígitos. */
    fun sanitize(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return ONLY_DIGITS.replace(input.trim(), "")
    }

    /** Valida se após sanitização o documento tem 11 (CPF) ou 14 (CNPJ) dígitos. */
    fun isValid(input: String?): Boolean {
        val digits = sanitize(input)
        return digits.length == 11 || digits.length == 14
    }

    /** Retorna os dígitos sanitizados ou lança IllegalArgumentException se inválido. */
    fun requireValid(input: String?): String {
        val digits = sanitize(input ?: "")
        if (digits.length != 11 && digits.length != 14) {
            throw IllegalArgumentException("Documento deve ter 11 dígitos (CPF) ou 14 dígitos (CNPJ). Recebido: ${digits.length} dígitos.")
        }
        return digits
    }
}
