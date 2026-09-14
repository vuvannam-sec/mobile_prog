package io.github.vuvannamsec.personalfinance.data.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.vuvannamsec.personalfinance.data.model.Transaction
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndReadTransaction() = runBlocking {
        val transaction = Transaction(
            amount = 42.5,
            category = "Food & Dining",
            type = "expense",
            note = "Lunch",
            date = 1_700_000_000_000,
            createdAt = 1_700_000_000_000
        )

        val id = database.transactionDao().insert(transaction)
        val stored = database.transactionDao().getTransactionById(id)

        assertNotNull(stored)
        assertEquals(42.5, stored?.amount ?: 0.0, 0.0)
        assertEquals("Food & Dining", stored?.category)
        assertEquals("expense", stored?.type)
        assertEquals("Lunch", stored?.note)
    }
}
