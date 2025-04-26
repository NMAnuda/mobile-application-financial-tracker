package com.example.labexam3

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.labexam3.databinding.FragmentStatisticsBinding
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType { INCOME, EXPENSE, SAVING }

data class Transaction(
    val type: TransactionType,
    val date: String,
    val categoryOrReason: String,
    val amount: String,
    val description: String?
)

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("financeData", Context.MODE_PRIVATE)

        // Apply fade-in animation to charts
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in)
        binding.incomeExpensePieChart.startAnimation(fadeIn)
        binding.categoryPieChart.startAnimation(fadeIn)
        binding.monthlyBalanceBarChart.startAnimation(fadeIn)

        setupCharts()
    }

    private fun setupCharts() {
        val transactions = loadAllTransactions()
        setupIncomeExpensePieChart(transactions)
        setupCategoryPieChart(transactions)
        setupMonthlyBalanceBarChart(transactions)
    }

    private fun loadAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val transactionString = sharedPreferences.getString("transaction_details", "") ?: ""
        val transactionList = transactionString.split("\n").filter { it.isNotBlank() }

        transactionList.forEach { transaction ->
            val parts = transaction.split(",")
            if (parts.size >= 4) {
                try {
                    dateFormat.parse(parts[1])?.let {
                        val type = when {
                            parts[0].equals("Income", ignoreCase = true) -> TransactionType.INCOME
                            parts[0].equals("Expense", ignoreCase = true) -> TransactionType.EXPENSE
                            parts[0].equals("Saving", ignoreCase = true) -> TransactionType.SAVING
                            else -> return@forEach
                        }
                        transactions.add(
                            Transaction(
                                type = type,
                                date = parts[1],
                                categoryOrReason = parts[2],
                                amount = parts[3],
                                description = parts.getOrNull(4)
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip invalid transactions
                }
            }
        }

        return transactions.sortedByDescending { dateFormat.parse(it.date) }
    }

    private fun setupIncomeExpensePieChart(transactions: List<Transaction>) {
        var incomeTotal = 0.0
        var expenseTotal = 0.0
        var savingTotal = 0.0

        transactions.forEach { transaction ->
            val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            when (transaction.type) {
                TransactionType.INCOME -> incomeTotal += amount
                TransactionType.EXPENSE -> expenseTotal += amount
                TransactionType.SAVING -> savingTotal += amount
            }
        }

        val entries = mutableListOf<ChartEntry>()
        if (incomeTotal > 0) entries.add(ChartEntry("Income", incomeTotal.toFloat(), Color.parseColor("#4CAF50")))
        if (expenseTotal > 0) entries.add(ChartEntry("Expenses", expenseTotal.toFloat(), Color.parseColor("#F44336")))
        if (savingTotal > 0) entries.add(ChartEntry("Savings", savingTotal.toFloat(), Color.parseColor("#FFC107")))

        if (entries.isEmpty()) {
            binding.incomeExpensePieChart.visibility = View.GONE
            binding.incomeExpensePlaceholder.visibility = View.VISIBLE
        } else {
            binding.incomeExpensePieChart.visibility = View.VISIBLE
            binding.incomeExpensePlaceholder.visibility = View.GONE
            binding.incomeExpensePieChart.setData(entries)
        }
    }

    private fun setupCategoryPieChart(transactions: List<Transaction>) {
        val categoryMap = mutableMapOf<String, Double>()
        val colors = listOf(
            Color.parseColor("#2196F3"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#009688"),
            Color.parseColor("#FFC107")
        )

        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
            val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val category = transaction.categoryOrReason
            categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + amount
        }

        val entries = categoryMap.entries.mapIndexed { index, entry ->
            ChartEntry(entry.key, entry.value.toFloat(), colors[index % colors.size])
        }

        if (entries.isEmpty()) {
            binding.categoryPieChart.visibility = View.GONE
            binding.categoryLegend.visibility = View.GONE
            binding.categoryPlaceholder.visibility = View.VISIBLE
        } else {
            binding.categoryPieChart.visibility = View.VISIBLE
            binding.categoryLegend.visibility = View.VISIBLE
            binding.categoryPlaceholder.visibility = View.GONE
            binding.categoryPieChart.setData(entries)

            // Populate the legend
            binding.categoryLegend.removeAllViews()
            entries.forEach { entry ->
                val legendItem = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 32, 0)
                    }
                }

                val colorDot = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(12, 12).apply {
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    setBackgroundColor(entry.color)
                }

                val label = TextView(requireContext()).apply {
                    text = entry.label
                    textSize = 14f
                    setTextColor(context.resources.getColor(R.color.text_secondary, null))
                    setPadding(8, 0, 0, 0)
                }

                legendItem.addView(colorDot)
                legendItem.addView(label)
                binding.categoryLegend.addView(legendItem)
            }
        }
    }

    private fun setupMonthlyBalanceBarChart(transactions: List<Transaction>) {
        val monthlyBalances = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            try {
                val date = dateFormat.parse(transaction.date)
                val monthKey = monthFormat.format(date)
                val amount = transaction.amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                val currentBalance = monthlyBalances.getOrDefault(monthKey, 0f)
                monthlyBalances[monthKey] = currentBalance +
                        when (transaction.type) {
                            TransactionType.INCOME -> amount
                            TransactionType.EXPENSE -> -amount
                            TransactionType.SAVING -> -amount
                        }.toFloat()
            } catch (e: Exception) {
                // Skip invalid transactions
            }
        }

        val entries = monthlyBalances.entries.sortedBy { monthFormat.parse(it.key) }.map { entry ->
            BarChartEntry(
                entry.key,
                entry.value,
                Color.parseColor("#2196F3")
            )
        }

        if (entries.isEmpty()) {
            binding.monthlyBalanceBarChart.visibility = View.GONE
            binding.monthlyBalancePlaceholder.visibility = View.VISIBLE
        } else {
            binding.monthlyBalanceBarChart.visibility = View.VISIBLE
            binding.monthlyBalancePlaceholder.visibility = View.GONE
            binding.monthlyBalanceBarChart.setData(entries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}