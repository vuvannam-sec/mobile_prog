package com.example.personalfinance.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.personalfinance.FinanceApplication
import com.example.personalfinance.data.model.Budget
import com.example.personalfinance.databinding.FragmentAddBudgetBinding
import com.example.personalfinance.viewmodel.BudgetViewModel
import com.example.personalfinance.viewmodel.BudgetViewModelFactory
import com.example.personalfinance.viewmodel.CategoryViewModel
import com.example.personalfinance.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

class AddBudgetFragment : Fragment() {

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!

    private val args: AddBudgetFragmentArgs by navArgs()

    private val budgetViewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(
            (requireActivity().application as FinanceApplication).budgetRepository
        )
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(
            (requireActivity().application as FinanceApplication).categoryRepository
        )
    }

    private var editingBudget: Budget? = null
    private var categoryList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setupClickListeners()

        if (args.budgetId != -1L) {
            loadBudget(args.budgetId)
        }
    }

    private fun setupCategorySpinner() {
        categoryViewModel.getCategoriesByType("expense").observe(viewLifecycleOwner) { categories ->
            categoryList = categories.map { it.name }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
            selectEditingCategory()
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveBudget()
        }
    }

    private fun loadBudget(id: Long) {
        lifecycleScope.launch {
            editingBudget = budgetViewModel.getBudgetById(id)
            editingBudget?.let { budget ->
                binding.etAmount.setText(budget.amount.toString())
                selectEditingCategory()
            }
        }
    }

    private fun selectEditingCategory() {
        val category = editingBudget?.category ?: return
        val categoryIndex = categoryList.indexOf(category)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }
    }

    private fun saveBudget() {
        val amountText = binding.etAmount.text.toString()
        val selectedCategory = binding.spinnerCategory.selectedItem?.toString()

        if (amountText.isEmpty()) {
            binding.tilAmount.error = "Please enter amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = "Please enter valid amount"
            return
        }

        if (selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        val budget = Budget(
            id = editingBudget?.id ?: 0,
            category = selectedCategory,
            amount = amount,
            month = editingBudget?.month ?: currentMonth,
            year = editingBudget?.year ?: currentYear
        )

        if (editingBudget != null) {
            budgetViewModel.update(budget)
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } else {
            budgetViewModel.insert(budget)
            Toast.makeText(requireContext(), "Budget added", Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
