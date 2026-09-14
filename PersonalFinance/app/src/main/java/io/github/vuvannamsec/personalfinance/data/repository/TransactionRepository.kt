package io.github.vuvannamsec.personalfinance.data.repository

import androidx.lifecycle.LiveData
import io.github.vuvannamsec.personalfinance.data.database.TransactionDao
import io.github.vuvannamsec.personalfinance.data.model.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    fun getTotalByType(type: String): LiveData<Double?> {
        return transactionDao.getTotalByType(type)
    }

    fun getTotalByTypeAndDateRange(type: String, startDate: Long, endDate: Long): LiveData<Double?> {
        return transactionDao.getTotalByTypeAndDateRange(type, startDate, endDate)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
}
