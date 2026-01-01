package com.example.personalfinance

import android.app.Application
import com.example.personalfinance.data.database.AppDatabase
import com.example.personalfinance.data.repository.BudgetRepository
import com.example.personalfinance.data.repository.CategoryRepository
import com.example.personalfinance.data.repository.TransactionRepository

class FinanceApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
}