package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "store_transactions")
data class StoreTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    
    @Column(nullable = false)
    var value: Double,
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var date: Date = Date(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: TransactionTypeEnum,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: TransactionCategory,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TransactionStatus = TransactionStatus.ACTIVE,
    
    @Column(nullable = false)
    var deleted: Boolean = false
)
