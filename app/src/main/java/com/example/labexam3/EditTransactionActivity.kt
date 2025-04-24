package com.example.labexam3

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var originalTransaction: String? = null
    private var originalAmount: Float = 0f
    private var originalType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        amountInput = findViewById(R.id.editAmountInput)
        descriptionInput = findViewById(R.id.editDescriptionInput)
        typeSpinner = findViewById(R.id.editTypeSpinner)
        saveButton = findViewById(R.id.updateButton)

        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val types = arrayOf("Income", "Expense", "Saving")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        originalTransaction = intent.getStringExtra("transaction")
        if (originalTransaction != null) {
            val parts = originalTransaction!!.split(": ", " - ")
            originalType = parts[0]
            originalAmount = parts[1].replace("Rs. ", "").toFloatOrNull() ?: 0f
            val desc = parts.getOrNull(2) ?: ""

            amountInput.setText(originalAmount.toString())
            descriptionInput.setText(desc)
            typeSpinner.setSelection(types.indexOf(originalType))
        }

        saveButton.setOnClickListener {
            updateTransaction()
        }
    }

    private fun updateTransaction() {
        val newAmount = amountInput.text.toString().toFloatOrNull() ?: return
        val newDescription = descriptionInput.text.toString().ifBlank { "No description" }
        val newType = typeSpinner.selectedItem.toString()

        var income = sharedPreferences.getFloat("income", 0f)
        var expenses = sharedPreferences.getFloat("expenses", 0f)
        var transactions = sharedPreferences.getString("transactions", "") ?: ""

        // Remove original amount
        when (originalType) {
            "Income" -> income -= originalAmount
            "Expense" -> expenses -= originalAmount
        }

        // Add new amount
        when (newType) {
            "Income" -> income += newAmount
            "Expense" -> expenses += newAmount
        }

        val updatedTransaction = "$newType: Rs. $newAmount - $newDescription"
        transactions = transactions.replace(originalTransaction!!, updatedTransaction)

        editor.putFloat("income", income)
        editor.putFloat("expenses", expenses)
        editor.putString("transactions", transactions)
        editor.apply()

        finish()
    }
}
