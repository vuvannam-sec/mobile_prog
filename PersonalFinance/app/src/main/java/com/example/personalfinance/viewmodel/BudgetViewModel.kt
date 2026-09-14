package com.example.personalfinance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.personalfinance.data.model.Budget
import com.example.personalfinance.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val allBudgets: LiveData<List<Budget>> = repository.allBudgets

    private val selectedPeriod = MutableLiveData(currentPeriod())

    val budgetsForSelectedPeriod: LiveData<List<Budget>> = selectedPeriod.switchMap { period ->
        repository.getBudgetsByMonth(period.month, period.year)
    }

    fun selectPeriod(month: Int, year: Int) {
        require(month in 1..12) { "month must be between 1 and 12" }

        val period = BudgetPeriod(month, year)
        if (selectedPeriod.value != period) {
            selectedPeriod.value = period
        }
    }

    fun getBudgetsByMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return repository.getBudgetsByMonth(month, year)
    }

    fun insert(budget: Budget) = viewModelScope.launch {
        repository.insert(budget)
    }

    fun update(budget: Budget) = viewModelScope.launch {
        repository.update(budget)
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        repository.delete(budget)
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return repository.getBudgetById(id)
    }

    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): Budget? {
        return repository.getBudgetByCategory(category, month, year)
    }

    private data class BudgetPeriod(val month: Int, val year: Int)

    companion object {
        private fun currentPeriod(): BudgetPeriod {
            val calendar = Calendar.getInstance()
            return BudgetPeriod(
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR)
            )
        }
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
