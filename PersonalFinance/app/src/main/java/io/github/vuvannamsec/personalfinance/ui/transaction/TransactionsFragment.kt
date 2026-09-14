package io.github.vuvannamsec.personalfinance.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vuvannamsec.personalfinance.FinanceApplication
import io.github.vuvannamsec.personalfinance.R
import io.github.vuvannamsec.personalfinance.data.model.Transaction
import io.github.vuvannamsec.personalfinance.databinding.FragmentTransactionsBinding
import io.github.vuvannamsec.personalfinance.ui.adapter.TransactionAdapter
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModelFactory

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var currentFilter = FILTER_ALL
    private var transactions: List<Transaction> = emptyList()

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
                checkedIds.contains(R.id.chip_income) -> FILTER_INCOME
                checkedIds.contains(R.id.chip_expense) -> FILTER_EXPENSE
                else -> FILTER_ALL
            }
            renderTransactions()
        }
    }

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { items ->
            transactions = items
            renderTransactions()
        }
    }

    private fun renderTransactions() {
        val visibleTransactions = when (currentFilter) {
            FILTER_INCOME -> transactions.filter { it.type == FILTER_INCOME }
            FILTER_EXPENSE -> transactions.filter { it.type == FILTER_EXPENSE }
            else -> transactions
        }

        transactionAdapter.submitList(visibleTransactions)
        updateEmptyState(visibleTransactions.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILTER_ALL = "all"
        private const val FILTER_INCOME = "income"
        private const val FILTER_EXPENSE = "expense"
    }
}
