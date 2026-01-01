package com.example.personalfinance.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.FinanceApplication
import com.example.personalfinance.R
import com.example.personalfinance.databinding.FragmentTransactionsBinding
import com.example.personalfinance.ui.adapter.TransactionAdapter
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupFilterChips()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val action = TransactionsFragmentDirections.actionTransactionsToTransactionDetail(transaction.id)
            findNavController().navigate(action)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_transactions_to_addTransaction)
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chip_income) -> "income"
                checkedIds.contains(R.id.chip_expense) -> "expense"
                else -> "all"
            }
            applyFilter()
        }
    }

    private fun applyFilter() {
        when (currentFilter) {
            "income" -> {
                viewModel.getTransactionsByType("income").observe(viewLifecycleOwner) { transactions ->
                    transactionAdapter.submitList(transactions)
                    updateEmptyState(transactions.isEmpty())
                }
            }
            "expense" -> {
                viewModel.getTransactionsByType("expense").observe(viewLifecycleOwner) { transactions ->
                    transactionAdapter.submitList(transactions)
                    updateEmptyState(transactions.isEmpty())
                }
            }
            else -> {
                viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
                    transactionAdapter.submitList(transactions)
                    updateEmptyState(transactions.isEmpty())
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            if (currentFilter == "all") {
                transactionAdapter.submitList(transactions)
                updateEmptyState(transactions.isEmpty())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}