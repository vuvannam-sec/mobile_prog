package io.github.vuvannamsec.personalfinance.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.vuvannamsec.personalfinance.R
import io.github.vuvannamsec.personalfinance.data.model.Transaction
import io.github.vuvannamsec.personalfinance.databinding.ItemTransactionBinding
import java.text.DateFormat
import java.util.Date

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                tvCategory.text = transaction.category
                tvNote.text = if (transaction.note.isNotEmpty()) {
                    transaction.note
                } else {
                    root.context.getString(R.string.no_note)
                }
                tvDate.text = formatDate(transaction.date)

                val amount = transaction.amount
                if (transaction.type == "income") {
                    tvAmount.text = root.context.getString(R.string.income_amount_format, amount)
                    tvAmount.setTextColor(root.context.getColor(R.color.income_green))
                } else {
                    tvAmount.text = root.context.getString(R.string.expense_amount_format, amount)
                    tvAmount.setTextColor(root.context.getColor(R.color.expense_red))
                }

                val categoryColor = getCategoryColor(transaction.category)
                viewCategoryColor.background.setTint(Color.parseColor(categoryColor))
            }
        }

        private fun formatDate(timestamp: Long): String {
            return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
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
                "Salary" -> "#4CAF50"
                "Business" -> "#FF9800"
                "Investment" -> "#00BCD4"
                "Gift" -> "#E91E63"
                else -> "#795548"
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
