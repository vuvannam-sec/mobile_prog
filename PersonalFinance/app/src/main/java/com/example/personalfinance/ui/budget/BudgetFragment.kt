package com.example.personalfinance.ui.budget

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
import com.example.personalfinance.data.model.Transaction
import com.example.personalfinance.databinding.FragmentBudgetBinding
import com.example.personalfinance.ui.adapter.BudgetAdapter
import com.example.personalfinance.viewmodel.BudgetViewModel
import com.example.personalfinance.viewmodel.BudgetViewModelFactory
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(
            (requireActivity().application as FinanceApplication).budgetRepository
        )
    }

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private lateinit var budgetAdapter: BudgetAdapter
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var transactionsList: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        updateMonthDisplay()
        observeData()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(
            onItemClick = { budget ->
                val action = BudgetFragmentDirections.actionBudgetToAddBudget(budget.id)
                findNavController().navigate(action)
            },
            getSpentAmount = { category ->
                calculateSpentAmount(category)
            }
        )

        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun calculateSpentAmount(category: String): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis

        return transactionsList
            .filter { it.type == "expense" && it.category == category && it.date in startOfMonth..endOfMonth }
            .sumOf { it.amount }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_budget_to_addBudget)
        }

        binding.ivPrevMonth.setOnClickListener {
            if (currentMonth == 1) {
                currentMonth = 12
                currentYear--
            } else {
                currentMonth--
            }
            updateMonthDisplay()
            loadBudgets()
        }

        binding.ivNextMonth.setOnClickListener {
            if (currentMonth == 12) {
                currentMonth = 1
                currentYear++
            } else {
                currentMonth++
            }
            updateMonthDisplay()
            loadBudgets()
        }
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth - 1)

        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonth.text = sdf.format(calendar.time)
    }

    private fun loadBudgets() {
        budgetViewModel.getBudgetsByMonth(currentMonth, currentYear).observe(viewLifecycleOwner) { budgets ->
            budgetAdapter.submitList(budgets)
            updateEmptyState(budgets.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvBudgets.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun observeData() {
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionsList = transactions
            budgetAdapter.notifyDataSetChanged()
        }

        loadBudgets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}