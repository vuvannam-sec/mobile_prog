package io.github.vuvannamsec.personalfinance

import android.app.Application
import io.github.vuvannamsec.personalfinance.data.database.AppDatabase
import io.github.vuvannamsec.personalfinance.data.repository.BudgetRepository
import io.github.vuvannamsec.personalfinance.data.repository.CategoryRepository
import io.github.vuvannamsec.personalfinance.data.repository.TransactionRepository

class FinanceApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
}
