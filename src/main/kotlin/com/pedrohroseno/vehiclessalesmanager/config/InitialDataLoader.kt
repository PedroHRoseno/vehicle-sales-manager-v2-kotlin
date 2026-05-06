package com.pedrohroseno.vehiclessalesmanager.config

import com.pedrohroseno.vehiclessalesmanager.model.User
import com.pedrohroseno.vehiclessalesmanager.model.enums.Role
import com.pedrohroseno.vehiclessalesmanager.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class InitialDataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${ADMIN_USERNAME:}") private val adminUsername: String,
    @Value("\${ADMIN_PASSWORD:}") private val adminPassword: String
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(InitialDataLoader::class.java)

    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val username = adminUsername.ifBlank { "admin" }
            val password = if (adminPassword.isNotBlank()) adminPassword else "admin123"
            val adminUser = User(
                username = username,
                password = passwordEncoder.encode(password),
                role = Role.ADMIN
            )
            userRepository.save(adminUser)
            if (adminUsername.isNotBlank() && adminPassword.isNotBlank()) {
                log.info("Usuário administrador criado a partir de variáveis de ambiente.")
            } else {
                log.warn("Usuário administrador padrão criado. Defina ADMIN_USERNAME e ADMIN_PASSWORD em produção.")
            }
        }
    }
}
