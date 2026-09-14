package io.github.vuvannamsec.personalfinance.ui.budget

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
import io.github.vuvannamsec.personalfinance.databinding.FragmentBudgetBinding
import io.github.vuvannamsec.personalfinance.domain.FinanceCalculator
import io.github.vuvannamsec.personalfinance.ui.adapter.BudgetAdapter
import io.github.vuvannamsec.personalfinance.viewmodel.BudgetViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.BudgetViewModelFactory
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.TransactionViewModelFactory
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
        budgetViewModel.selectPeriod(currentMonth, currentYear)
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(
            onItemClick = { budget ->
                val action = BudgetFragmentDirections.actionBudgetToAddBudget(budget.id)
                findNavController().navigate(action)
            },
            getSpentAmount = { category -> calculateSpentAmount(category) }
        )

        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun calculateSpentAmount(category: String): Double {
        val (startOfMonth, endOfMonth) = monthBounds(currentMonth, currentYear)
        return FinanceCalculator.spentForCategory(
            transactions = transactionsList,
            category = category,
            startInclusive = startOfMonth,
            endInclusive = endOfMonth
        )
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
            onPeriodChanged()
        }

        binding.ivNextMonth.setOnClickListener {
            if (currentMonth == 12) {
                currentMonth = 1
                currentYear++
            } else {
                currentMonth++
            }
            onPeriodChanged()
        }
    }

    private fun onPeriodChanged() {
        updateMonthDisplay()
        budgetViewModel.selectPeriod(currentMonth, currentYear)
        budgetAdapter.notifyDataSetChanged()
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
        }

        binding.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
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

        budgetViewModel.budgetsForSelectedPeriod.observe(viewLifecycleOwner) { budgets ->
            budgetAdapter.submitList(budgets)
            updateEmptyState(budgets.isEmpty())
        }
    }

    private fun monthBounds(month: Int, year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return start to calendar.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
