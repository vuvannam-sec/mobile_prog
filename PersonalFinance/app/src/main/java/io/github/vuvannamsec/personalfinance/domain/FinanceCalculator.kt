package io.github.vuvannamsec.personalfinance.domain

import io.github.vuvannamsec.personalfinance.data.model.Transaction

data class FinanceSummary(
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense
}

object FinanceCalculator {

    fun summarize(transactions: List<Transaction>): FinanceSummary {
        var income = 0.0
        var expense = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "income" -> income += transaction.amount
                "expense" -> expense += transaction.amount
            }
        }

        return FinanceSummary(income = income, expense = expense)
    }

    fun expensesByCategory(transactions: List<Transaction>): Map<String, Double> {
        return transactions
            .asSequence()
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }

    fun spentForCategory(
        transactions: List<Transaction>,
        category: String,
        startInclusive: Long,
        endInclusive: Long
    ): Double {
        require(startInclusive <= endInclusive) { "startInclusive must not be after endInclusive" }

        return transactions
            .asSequence()
            .filter {
                it.type == "expense" &&
                    it.category == category &&
                    it.date in startInclusive..endInclusive
            }
            .sumOf { it.amount }
    }
}
