package com.pedrohroseno.vehiclessalesmanager.config

import org.slf4j.LoggerFactory
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(PropertyReferenceException::class)
    fun handleInvalidSort(ex: PropertyReferenceException): ResponseEntity<Map<String, String>> {
        log.warn("Ordenação inválida: {}", ex.message)
        return ResponseEntity.badRequest().body(
            mapOf("message" to "Parâmetro de ordenação inválido: ${ex.propertyName}")
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, String>> {
        log.warn("Parâmetro inválido: {}", ex.message)
        return ResponseEntity.badRequest().body(
            mapOf("message" to "Parâmetro inválido: ${ex.name}")
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("message" to (ex.message ?: "Requisição inválida")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, String>> {
        log.error("Erro não tratado", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            mapOf("message" to (ex.message ?: "Erro interno do servidor"))
        )
    }
}
