package com.example.personalfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: String,
    val note: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)