package io.github.vuvannamsec.personalfinance.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.vuvannamsec.personalfinance.FinanceApplication
import io.github.vuvannamsec.personalfinance.R
import io.github.vuvannamsec.personalfinance.data.model.Transaction
import io.github.vuvannamsec.personalfinance.databinding.FragmentTransactionDetailBinding
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: TransactionDetailFragmentArgs by navArgs()

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private var currentTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTransaction()
        setupClickListeners()
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            currentTransaction = viewModel.getTransactionById(args.transactionId)
            currentTransaction?.let { transaction ->
                displayTransaction(transaction)
            }
        }
    }

    private fun displayTransaction(transaction: Transaction) {
        binding.apply {
            val isIncome = transaction.type == "income"
            tvType.text = getString(if (isIncome) R.string.income else R.string.expense)

            if (isIncome) {
                tvAmount.text = getString(R.string.income_amount_format, transaction.amount)
                tvAmount.setTextColor(requireContext().getColor(R.color.income_green))
                tvType.setTextColor(requireContext().getColor(R.color.income_green))
            } else {
                tvAmount.text = getString(R.string.expense_amount_format, transaction.amount)
                tvAmount.setTextColor(requireContext().getColor(R.color.expense_red))
                tvType.setTextColor(requireContext().getColor(R.color.expense_red))
            }

            tvCategory.text = transaction.category
            tvDate.text = formatDate(transaction.date)
            tvNote.text = if (transaction.note.isNotEmpty()) {
                transaction.note
            } else {
                getString(R.string.no_note)
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return DateFormat.getDateInstance(DateFormat.LONG).format(Date(timestamp))
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            currentTransaction?.let { transaction ->
                val action = TransactionDetailFragmentDirections
                    .actionTransactionDetailFragmentToAddTransactionFragment(transaction.id)
                findNavController().navigate(action)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction_title)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.yes) { _, _ ->
                currentTransaction?.let { transaction ->
                    viewModel.delete(transaction)
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
