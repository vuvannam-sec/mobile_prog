package io.github.vuvannamsec.personalfinance.ui.budget

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
import io.github.vuvannamsec.personalfinance.FinanceApplication
import io.github.vuvannamsec.personalfinance.R
import io.github.vuvannamsec.personalfinance.data.model.Budget
import io.github.vuvannamsec.personalfinance.databinding.FragmentAddBudgetBinding
import io.github.vuvannamsec.personalfinance.viewmodel.BudgetViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.BudgetViewModelFactory
import io.github.vuvannamsec.personalfinance.viewmodel.CategoryViewModel
import io.github.vuvannamsec.personalfinance.viewmodel.CategoryViewModelFactory
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
        val amountText = binding.etAmount.text.toString().trim()
        val selectedCategory = binding.spinnerCategory.selectedItem?.toString()

        if (amountText.isEmpty()) {
            binding.tilAmount.error = getString(R.string.amount_required)
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = getString(R.string.amount_invalid)
            return
        }
        binding.tilAmount.error = null

        if (selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.category_required, Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        val fallbackMonth = calendar.get(Calendar.MONTH) + 1
        val fallbackYear = calendar.get(Calendar.YEAR)
        val targetMonth = editingBudget?.month
            ?: args.month.takeIf { it in 1..12 }
            ?: fallbackMonth
        val targetYear = editingBudget?.year
            ?: args.year.takeIf { it > 0 }
            ?: fallbackYear

        val budget = Budget(
            id = editingBudget?.id ?: 0,
            category = selectedCategory,
            amount = amount,
            month = targetMonth,
            year = targetYear
        )

        if (editingBudget != null) {
            budgetViewModel.update(budget)
            Toast.makeText(requireContext(), R.string.budget_updated, Toast.LENGTH_SHORT).show()
        } else {
            budgetViewModel.insert(budget)
            Toast.makeText(requireContext(), R.string.budget_added, Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
