package com.example.personalfinance.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.personalfinance.FinanceApplication
import com.example.personalfinance.R
import com.example.personalfinance.data.model.Transaction
import com.example.personalfinance.databinding.FragmentTransactionDetailBinding
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            tvType.text = transaction.type.replaceFirstChar { it.uppercase() }

            if (transaction.type == "income") {
                tvAmount.text = String.format("+$%,.2f", transaction.amount)
                tvAmount.setTextColor(requireContext().getColor(R.color.income_green))
                tvType.setTextColor(requireContext().getColor(R.color.income_green))
            } else {
                tvAmount.text = String.format("-$%,.2f", transaction.amount)
                tvAmount.setTextColor(requireContext().getColor(R.color.expense_red))
                tvType.setTextColor(requireContext().getColor(R.color.expense_red))
            }

            tvCategory.text = transaction.category
            tvDate.text = formatDate(transaction.date)
            tvNote.text = if (transaction.note.isNotEmpty()) transaction.note else "No note"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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
            .setTitle("Delete Transaction")
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                currentTransaction?.let { transaction ->
                    viewModel.delete(transaction)
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}