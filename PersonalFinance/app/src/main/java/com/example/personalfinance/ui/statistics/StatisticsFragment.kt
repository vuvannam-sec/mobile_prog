package com.example.personalfinance.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.personalfinance.FinanceApplication
import com.example.personalfinance.R
import com.example.personalfinance.databinding.FragmentStatisticsBinding
import com.example.personalfinance.viewmodel.TransactionViewModel
import com.example.personalfinance.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart()
        setupBarChart()
        observeData()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(16f)
            animateY(1000, Easing.EaseInOutQuad)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 10f
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            animateY(1000)

            xAxis.apply {
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                setLabelCount(2, true)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            // Calculate expense by category
            val expenseByCategory = transactions
                .filter { it.type == "expense" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            updatePieChart(expenseByCategory)

            // Calculate total income and expense
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

            updateBarChart(totalIncome, totalExpense)
            updateSummary(totalIncome, totalExpense)
        }
    }

    private fun updatePieChart(expenseByCategory: Map<String, Double>) {
        if (expenseByCategory.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "No Data"
            return
        }

        val entries = expenseByCategory.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val colors = listOf(
            Color.parseColor("#FF5722"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#795548")
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(binding.pieChart)
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            centerText = "Expenses"
            invalidate()
        }
    }

    private fun updateBarChart(income: Double, expense: Double) {
        val entries = listOf(
            BarEntry(0f, income.toFloat()),
            BarEntry(1f, expense.toFloat())
        )

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(
                requireContext().getColor(R.color.income_green),
                requireContext().getColor(R.color.expense_red)
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.5f
            }
            xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        0 -> "Income"
                        1 -> "Expense"
                        else -> ""
                    }
                }
            }
            invalidate()
        }
    }

    private fun updateSummary(income: Double, expense: Double) {
        binding.tvTotalIncome.text = String.format("$%,.2f", income)
        binding.tvTotalExpense.text = String.format("$%,.2f", expense)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}