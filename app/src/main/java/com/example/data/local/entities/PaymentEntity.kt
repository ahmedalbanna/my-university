package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "TUITION", "PRIVATE_TUITION", "TELECOM", "OTHER"
    val amount: Double,
    val currency: String = "ر.س",
    val status: String, // "PAID", "PENDING", "FAILED"
    val date: String,
    val referenceNo: String,
    val paymentMethod: String
)
