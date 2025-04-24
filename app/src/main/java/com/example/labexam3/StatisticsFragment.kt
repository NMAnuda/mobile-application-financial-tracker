package com.example.labexam3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalIncomeText = view.findViewById<TextView>(R.id.totalIncome)
        val totalExpenseText = view.findViewById<TextView>(R.id.totalExpense)
        val netBalanceText = view.findViewById<TextView>(R.id.netBalance)

        // Replace these with values from SharedPreferences if needed
        val income = 25000.0
        val expense = 10000.0
        val net = income - expense

        totalIncomeText.text = "Total Income: Rs. %.2f".format(income)
        totalExpenseText.text = "Total Expense: Rs. %.2f".format(expense)
        netBalanceText.text = "Net Balance: Rs. %.2f".format(net)
    }
}
