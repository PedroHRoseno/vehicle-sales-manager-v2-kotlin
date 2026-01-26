package com.pedrohroseno.vehiclessalesmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    @Value("\${spring.web.cors.allowed-origins:http://localhost:3000}")
    private lateinit var allowedOrigins: String

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        
        // Se for "*", permitir qualquer origem (mas não pode usar credentials)
        if (allowedOrigins.trim() == "*") {
            config.addAllowedOriginPattern("*")
            config.allowCredentials = false
        } else {
            // Suporta múltiplas origens separadas por vírgula
            val origins = allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (origins.isEmpty()) {
                // Fallback para localhost se vazio
                config.addAllowedOrigin("http://localhost:3000")
            } else {
                origins.forEach { origin ->
                    config.addAllowedOrigin(origin)
                }
            }
            config.allowCredentials = true
        }
        
        // Permitir todos os métodos HTTP necessários
        config.addAllowedMethod("GET")
        config.addAllowedMethod("POST")
        config.addAllowedMethod("PUT")
        config.addAllowedMethod("DELETE")
        config.addAllowedMethod("OPTIONS")
        config.addAllowedMethod("PATCH")
        
        // Permitir todos os headers
        config.addAllowedHeader("*")
        
        // Headers expostos na resposta
        config.addExposedHeader("*")
        
        // Tempo de cache para preflight requests (1 hora)
        config.maxAge = 3600L
        
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
