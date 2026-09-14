package com.example.personalfinance.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personalfinance.FinanceApplication
import com.example.personalfinance.R
import com.example.personalfinance.data.model.Category
import com.example.personalfinance.data.model.Transaction
import com.example.personalfinance.databinding.FragmentAddTransactionBinding
import com.example.personalfinance.ui.adapter.CategoryAdapter
import com.example.personalfinance.viewmodel.CategoryViewModel
import com.example.personalfinance.viewmodel.CategoryViewModelFactory
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val args: AddTransactionFragmentArgs by navArgs()

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(
            (requireActivity().application as FinanceApplication).categoryRepository
        )
    }

    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategory: Category? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private var currentType = "expense"
    private var editingTransaction: Transaction? = null
    private var pendingCategoryName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryRecyclerView()
        setupClickListeners()
        setupTypeToggle()
        updateDateDisplay()
        observeCategories()

        if (args.transactionId != -1L) {
            loadTransaction(args.transactionId)
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            selectedCategory = category
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.tvDate.setOnClickListener { showDatePicker() }
        binding.ivCalendar.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveTransaction() }
    }

    private fun setupTypeToggle() {
        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val newType = if (checkedId == R.id.btn_income) "income" else "expense"
            if (newType != currentType) {
                currentType = newType
                selectedCategory = null
                categoryAdapter.setSelectedCategory("")
            }

            categoryViewModel.selectType(currentType)
        }
    }

    private fun observeCategories() {
        categoryViewModel.categoriesForSelectedType.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories) {
                applyPendingCategorySelection()
            }
        }
    }

    private fun applyPendingCategorySelection() {
        val categoryName = pendingCategoryName ?: return
        val category = categoryAdapter.currentList.firstOrNull { it.name == categoryName } ?: return

        selectedCategory = category
        categoryAdapter.setSelectedCategory(categoryName)
        pendingCategoryName = null
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date(selectedDate))
    }

    private fun loadTransaction(id: Long) {
        lifecycleScope.launch {
            editingTransaction = transactionViewModel.getTransactionById(id)
            editingTransaction?.let { transaction ->
                binding.etAmount.setText(transaction.amount.toString())
                binding.etNote.setText(transaction.note)
                selectedDate = transaction.date
                updateDateDisplay()

                pendingCategoryName = transaction.category
                currentType = transaction.type
                binding.toggleType.check(
                    if (transaction.type == "income") R.id.btn_income else R.id.btn_expense
                )
                categoryViewModel.selectType(currentType)
                applyPendingCategorySelection()
            }
        }
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (amountText.isEmpty()) {
            binding.tilAmount.error = "Please enter amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = "Please enter valid amount"
            return
        }
        binding.tilAmount.error = null

        val category = selectedCategory
        if (category == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = editingTransaction?.id ?: 0,
            amount = amount,
            category = category.name,
            type = currentType,
            note = note,
            date = selectedDate
        )

        if (editingTransaction != null) {
            transactionViewModel.update(transaction)
            Toast.makeText(requireContext(), "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            transactionViewModel.insert(transaction)
            Toast.makeText(requireContext(), "Transaction added", Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
