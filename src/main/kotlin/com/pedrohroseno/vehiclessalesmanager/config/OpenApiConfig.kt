package com.pedrohroseno.vehiclessalesmanager.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Vehicle Sales Manager API")
                    .version("2.0.0")
                    .description("API para gerenciamento de vendas, compras e trocas de veículos")
            )
            .addServersItem(
                Server()
                    .url("/")
                    .description("Servidor atual")
            )
    }
}
