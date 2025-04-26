package com.example.labexam3

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var amountInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var categoryInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        amountInput = findViewById(R.id.amountInput)
        dateInput = findViewById(R.id.dateInput)
        categoryInput = findViewById(R.id.categoryInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        typeSpinner = findViewById(R.id.typeSpinner)

        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Set default date to today
        dateInput.setText(dateFormat.format(Date()))

        val types = arrayOf("Income", "Expense", "Saving")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amount = amountInput.text.toString().toFloatOrNull()
        if (amount == null || amount <= 0) {
            amountInput.error = "Enter a valid amount"
            return
        }

        val date = dateInput.text.toString().ifBlank { dateFormat.format(Date()) }
        val category = categoryInput.text.toString().ifBlank { "Uncategorized" }
        val description = descriptionInput.text.toString().ifBlank { "No description" }
        val type = typeSpinner.selectedItem.toString()

        // Validate date format
        try {
            dateFormat.parse(date) ?: return
        } catch (e: Exception) {
            dateInput.error = "Invalid date format (use yyyy-MM-dd)"
            return
        }

        // Fetch financial data
        var income = sharedPreferences.getFloat("income", 0f)
        var expenses = sharedPreferences.getFloat("expenses", 0f)
        var savings = sharedPreferences.getFloat("savings", 0f)
        val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)

        // Calculate budget and remaining budget
        val budget = income * (budgetPercentage / 100f)
        val remainingBudget = budget - expenses

        // Check if expense exceeds remaining budget
        if (type == "Expense" && amount > remainingBudget) {
            Toast.makeText(this, "Expense exceeds remaining budget (Rs. $remainingBudget)", Toast.LENGTH_LONG).show()
            return
        }

        var transactions = sharedPreferences.getString("transactions", "") ?: ""
        var transactionDetails = sharedPreferences.getString("transaction_details", "") ?: ""

        when (type) {
            "Income" -> income += amount
            "Expense" -> expenses += amount
            "Saving" -> savings += amount
        }

        // Original format for HomeFragment: type: Rs. amount - description
        val newTransaction = "$type: Rs. $amount - $description\n"
        transactions = newTransaction + transactions

        // Detailed format for StatisticsFragment: type,date,category,amount,description
        val newTransactionDetail = "$type,$date,$category,$amount,$description\n"
        transactionDetails = newTransactionDetail + transactionDetails

        editor.putFloat("income", income)
        editor.putFloat("expenses", expenses)
        editor.putFloat("savings", savings)
        editor.putString("transactions", transactions)
        editor.putString("transaction_details", transactionDetails)
        editor.apply()

        Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}