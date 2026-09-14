package io.github.vuvannamsec.personalfinance.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.vuvannamsec.personalfinance.data.model.Budget
import io.github.vuvannamsec.personalfinance.data.model.Category
import io.github.vuvannamsec.personalfinance.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        private const val DATABASE_NAME = "personal_finance_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategories(database.categoryDao())
                }
            }
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Food & Dining", icon = "ic_food", color = "#FF5722", type = "expense"),
                Category(name = "Transportation", icon = "ic_transport", color = "#2196F3", type = "expense"),
                Category(name = "Shopping", icon = "ic_shopping", color = "#E91E63", type = "expense"),
                Category(name = "Entertainment", icon = "ic_entertainment", color = "#9C27B0", type = "expense"),
                Category(name = "Bills & Utilities", icon = "ic_bills", color = "#607D8B", type = "expense"),
                Category(name = "Healthcare", icon = "ic_health", color = "#4CAF50", type = "expense"),
                Category(name = "Education", icon = "ic_education", color = "#3F51B5", type = "expense"),
                Category(name = "Other Expense", icon = "ic_other", color = "#795548", type = "expense"),
                Category(name = "Salary", icon = "ic_salary", color = "#4CAF50", type = "income"),
                Category(name = "Business", icon = "ic_business", color = "#FF9800", type = "income"),
                Category(name = "Investment", icon = "ic_investment", color = "#00BCD4", type = "income"),
                Category(name = "Gift", icon = "ic_gift", color = "#E91E63", type = "income"),
                Category(name = "Other Income", icon = "ic_other", color = "#9E9E9E", type = "income")
            )
            categoryDao.insertAll(defaultCategories)
        }
    }
}
