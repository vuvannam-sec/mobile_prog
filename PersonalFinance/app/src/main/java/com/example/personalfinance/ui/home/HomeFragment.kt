package com.example.personalfinance.ui.home

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
import com.example.personalfinance.databinding.FragmentHomeBinding
import com.example.personalfinance.ui.adapter.TransactionAdapter
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            val action = HomeFragmentDirections.actionHomeToTransactionDetail(transaction.id)
            findNavController().navigate(action)
        }

        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addTransaction)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.transactionsFragment)
        }
    }

    private fun observeData() {
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.tvBalance.text = String.format("$%,.2f", balance ?: 0.0)
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = String.format("$%,.2f", income ?: 0.0)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = String.format("$%,.2f", expense ?: 0.0)
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val recentTransactions = transactions.take(5)
            transactionAdapter.submitList(recentTransactions)

            binding.tvEmpty.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRecentTransactions.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}