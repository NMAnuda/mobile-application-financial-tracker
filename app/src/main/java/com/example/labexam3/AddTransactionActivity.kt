package com.example.labexam3

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        typeSpinner = findViewById(R.id.typeSpinner)

        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val types = arrayOf("Income", "Expense", "Saving")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amount = amountInput.text.toString().toFloatOrNull() ?: return
        val description = descriptionInput.text.toString().ifBlank { "No description" }
        val type = typeSpinner.selectedItem.toString()

        var income = sharedPreferences.getFloat("income", 0f)
        var expenses = sharedPreferences.getFloat("expenses", 0f)
        var transactions = sharedPreferences.getString("transactions", "") ?: ""

        when (type) {
            "Income" -> income += amount
            "Expense" -> expenses += amount
        }

        val newTransaction = "$type: Rs. $amount - $description\n"
        transactions = newTransaction + transactions

        editor.putFloat("income", income)
        editor.putFloat("expenses", expenses)
        editor.putString("transactions", transactions)
        editor.apply()

        finish()
    }
}
