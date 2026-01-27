package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.StoreTransaction
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface StoreTransactionRepository : JpaRepository<StoreTransaction, Long> {
    fun findByIdAndDeletedFalse(id: Long): StoreTransaction?
    
    @Query("SELECT st FROM StoreTransaction st WHERE st.deleted = false ORDER BY st.date DESC")
    fun findAllByDeletedFalseOrderByDateDesc(pageable: Pageable): Page<StoreTransaction>
    
    @Query("SELECT st FROM StoreTransaction st WHERE st.deleted = false AND st.date BETWEEN :startDate AND :endDate ORDER BY st.date DESC")
    fun findAllByDateRangeAndDeletedFalse(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date, pageable: Pageable): Page<StoreTransaction>
    
    @Query("SELECT st FROM StoreTransaction st WHERE st.deleted = false AND st.type = :type ORDER BY st.date DESC")
    fun findAllByTypeAndDeletedFalse(@Param("type") type: TransactionTypeEnum, pageable: Pageable): Page<StoreTransaction>
    
    @Query("SELECT st FROM StoreTransaction st WHERE st.deleted = false AND st.category = :category ORDER BY st.date DESC")
    fun findAllByCategoryAndDeletedFalse(@Param("category") category: TransactionCategory, pageable: Pageable): Page<StoreTransaction>
    
    @Query("SELECT COALESCE(SUM(st.value), 0) FROM StoreTransaction st WHERE st.deleted = false AND st.status = :status AND st.type = :type")
    fun sumByStatusAndType(@Param("status") status: TransactionStatus, @Param("type") type: TransactionTypeEnum): Double
    
    @Query("SELECT COALESCE(SUM(st.value), 0) FROM StoreTransaction st WHERE st.deleted = false AND st.status = :status AND st.type = :type AND st.category IN :categories")
    fun sumByStatusTypeAndCategories(@Param("status") status: TransactionStatus, @Param("type") type: TransactionTypeEnum, @Param("categories") categories: List<TransactionCategory>): Double
}
