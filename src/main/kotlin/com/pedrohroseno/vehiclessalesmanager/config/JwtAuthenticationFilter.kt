package com.pedrohroseno.vehiclessalesmanager.config

import com.pedrohroseno.vehiclessalesmanager.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    /** Não aplicar o filtro em preflight CORS — OPTIONS deve passar sempre. */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return HttpMethod.OPTIONS.matches(request.method)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7).trim()

            if (jwtService.validateToken(token)) {
                val username = jwtService.getUsernameFromToken(token)
                val role = jwtService.getRoleFromToken(token)

                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val authentication = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            } else {
                // Ajuda a diferenciar "sem token" de "token inválido" nos logs do Railway.
                log.warn("JWT inválido em {} {} (header com {} chars)", request.method, request.requestURI, authHeader.length)
            }
        }

        filterChain.doFilter(request, response)
    }
}
