package com.example.personalfinance.viewmodel

import androidx.lifecycle.*
import com.example.personalfinance.data.model.Budget
import com.example.personalfinance.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.util.*

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val allBudgets: LiveData<List<Budget>> = repository.allBudgets

    private val _currentMonthBudgets = MutableLiveData<List<Budget>>()
    val currentMonthBudgets: LiveData<List<Budget>> = _currentMonthBudgets

    init {
        loadCurrentMonthBudgets()
    }

    private fun loadCurrentMonthBudgets() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        repository.getBudgetsByMonth(month, year).observeForever { budgets ->
            _currentMonthBudgets.value = budgets
        }
    }

    fun getBudgetsByMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return repository.getBudgetsByMonth(month, year)
    }

    fun insert(budget: Budget) = viewModelScope.launch {
        repository.insert(budget)
        loadCurrentMonthBudgets()
    }

    fun update(budget: Budget) = viewModelScope.launch {
        repository.update(budget)
        loadCurrentMonthBudgets()
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        repository.delete(budget)
        loadCurrentMonthBudgets()
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return repository.getBudgetById(id)
    }

    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): Budget? {
        return repository.getBudgetByCategory(category, month, year)
    }
}

class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}