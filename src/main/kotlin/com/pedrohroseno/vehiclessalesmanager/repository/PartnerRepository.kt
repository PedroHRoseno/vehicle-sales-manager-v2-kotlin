package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Partner
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PartnerRepository : JpaRepository<Partner, String> {
    fun findByDocument(document: String): Partner?
    fun findAllByOrderByNameAsc(pageable: Pageable): Page<Partner>
    
    @Query("SELECT p FROM Partner p WHERE " +
           "LOWER(p.document) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY p.name ASC")
    fun searchByDocumentOrName(@Param("search") search: String, pageable: Pageable): Page<Partner>
}
