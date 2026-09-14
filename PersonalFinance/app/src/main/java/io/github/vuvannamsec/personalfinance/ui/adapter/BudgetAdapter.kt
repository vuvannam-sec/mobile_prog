package io.github.vuvannamsec.personalfinance.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.vuvannamsec.personalfinance.R
import io.github.vuvannamsec.personalfinance.data.model.Budget
import io.github.vuvannamsec.personalfinance.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onItemClick: (Budget) -> Unit,
    private val getSpentAmount: (String) -> Double
) : ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BudgetViewHolder(
        private val binding: ItemBudgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(budget: Budget) {
            binding.apply {
                tvCategory.text = budget.category
                tvAmount.text = root.context.getString(R.string.currency_format, budget.amount)

                val spent = getSpentAmount(budget.category)
                val remaining = budget.amount - spent
                val progress = if (budget.amount > 0) ((spent / budget.amount) * 100).toInt() else 0

                tvSpent.text = root.context.getString(R.string.spent_amount_format, spent)

                if (remaining >= 0) {
                    tvRemaining.text = root.context.getString(R.string.remaining_amount_format, remaining)
                    tvRemaining.setTextColor(root.context.getColor(R.color.income_green))
                    progressBudget.setIndicatorColor(root.context.getColor(R.color.income_green))
                } else {
                    tvRemaining.text = root.context.getString(R.string.over_amount_format, -remaining)
                    tvRemaining.setTextColor(root.context.getColor(R.color.expense_red))
                    progressBudget.setIndicatorColor(root.context.getColor(R.color.expense_red))
                }

                progressBudget.progress = progress.coerceAtMost(100)

                val categoryColor = getCategoryColor(budget.category)
                viewCategoryColor.background.setTint(Color.parseColor(categoryColor))
            }
        }

        private fun getCategoryColor(category: String): String {
            return when (category) {
                "Food & Dining" -> "#FF5722"
                "Transportation" -> "#2196F3"
                "Shopping" -> "#E91E63"
                "Entertainment" -> "#9C27B0"
                "Bills & Utilities" -> "#607D8B"
                "Healthcare" -> "#4CAF50"
                "Education" -> "#3F51B5"
                else -> "#795548"
            }
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem == newItem
        }
    }
}
