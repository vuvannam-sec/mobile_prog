package io.github.vuvannamsec.personalfinance.domain

import io.github.vuvannamsec.personalfinance.data.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FinanceCalculatorTest {

    @Test
    fun summarizeSeparatesIncomeExpenseAndBalance() {
        val transactions = listOf(
            transaction(amount = 2400.0, type = "income", category = "Salary"),
            transaction(amount = 325.5, type = "expense", category = "Food & Dining"),
            transaction(amount = 74.5, type = "expense", category = "Transportation")
        )

        val summary = FinanceCalculator.summarize(transactions)

        assertEquals(2400.0, summary.income, 0.0)
        assertEquals(400.0, summary.expense, 0.0)
        assertEquals(2000.0, summary.balance, 0.0)
    }

    @Test
    fun expensesByCategoryExcludesIncome() {
        val transactions = listOf(
            transaction(amount = 80.0, type = "expense", category = "Food & Dining"),
            transaction(amount = 20.0, type = "expense", category = "Food & Dining"),
            transaction(amount = 500.0, type = "income", category = "Food & Dining"),
            transaction(amount = 45.0, type = "expense", category = "Transportation")
        )

        val totals = FinanceCalculator.expensesByCategory(transactions)

        assertEquals(100.0, totals.getValue("Food & Dining"), 0.0)
        assertEquals(45.0, totals.getValue("Transportation"), 0.0)
        assertEquals(2, totals.size)
    }

    @Test
    fun spentForCategoryRespectsTypeCategoryAndDateRange() {
        val transactions = listOf(
            transaction(30.0, "expense", "Food & Dining", date = 100),
            transaction(25.0, "expense", "Food & Dining", date = 200),
            transaction(90.0, "expense", "Transportation", date = 150),
            transaction(500.0, "income", "Food & Dining", date = 150),
            transaction(40.0, "expense", "Food & Dining", date = 400)
        )

        val spent = FinanceCalculator.spentForCategory(
            transactions = transactions,
            category = "Food & Dining",
            startInclusive = 100,
            endInclusive = 300
        )

        assertEquals(55.0, spent, 0.0)
    }

    @Test
    fun spentForCategoryRejectsReversedRange() {
        assertThrows(IllegalArgumentException::class.java) {
            FinanceCalculator.spentForCategory(
                transactions = emptyList(),
                category = "Food & Dining",
                startInclusive = 200,
                endInclusive = 100
            )
        }
    }

    private fun transaction(
        amount: Double,
        type: String,
        category: String,
        date: Long = 0
    ) = Transaction(
        amount = amount,
        category = category,
        type = type,
        note = "",
        date = date,
        createdAt = 0
    )
}
