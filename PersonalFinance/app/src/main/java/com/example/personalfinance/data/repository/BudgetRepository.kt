package com.example.personalfinance.data.repository

import androidx.lifecycle.LiveData
import com.example.personalfinance.data.database.BudgetDao
import com.example.personalfinance.data.model.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetsByMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByMonth(month, year)
    }

    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): Budget? {
        return budgetDao.getBudgetByCategory(category, month, year)
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)
    }

    suspend fun insert(budget: Budget): Long {
        return budgetDao.insert(budget)
    }

    suspend fun update(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun delete(budget: Budget) {
        budgetDao.delete(budget)
    }
}