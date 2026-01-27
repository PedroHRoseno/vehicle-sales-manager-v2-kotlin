package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.ActionType
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionType
import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "transaction_history")
data class TransactionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var transactionType: TransactionType,
    
    @Column(nullable = false)
    var transactionId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var actionType: ActionType,
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var actionDate: Date = Date(),
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var oldValue: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var newValue: String? = null,
    
    var performedBy: String? = null
)
