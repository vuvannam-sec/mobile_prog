package com.example.personalfinance.viewmodel

import androidx.lifecycle.*
import com.example.personalfinance.data.model.Transaction
import com.example.personalfinance.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.*

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _currentMonthTransactions = MutableLiveData<List<Transaction>>()
    val currentMonthTransactions: LiveData<List<Transaction>> = _currentMonthTransactions

    init {
        loadCurrentMonthData()
    }

    private fun loadCurrentMonthData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis

        repository.getTotalByTypeAndDateRange("income", startOfMonth, endOfMonth)
            .observeForever { income ->
                _totalIncome.value = income ?: 0.0
                updateBalance()
            }

        repository.getTotalByTypeAndDateRange("expense", startOfMonth, endOfMonth)
            .observeForever { expense ->
                _totalExpense.value = expense ?: 0.0
                updateBalance()
            }
    }

    private fun updateBalance() {
        val income = _totalIncome.value ?: 0.0
        val expense = _totalExpense.value ?: 0.0
        _balance.value = income - expense
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByDateRange(startDate, endDate)
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
        loadCurrentMonthData()
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
        loadCurrentMonthData()
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
        loadCurrentMonthData()
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return repository.getTransactionById(id)
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}