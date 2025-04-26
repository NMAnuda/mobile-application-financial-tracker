package com.example.labexam3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val TAG = "FragmentSettings"

    // Activity result launchers for file picker (restore) and file creation (backup)
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreData(uri)
            } ?: Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                backupData(uri)
            } ?: Toast.makeText(context, "No location selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("financeData", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Setup buttons
        val backupButton = view.findViewById<Button>(R.id.backupButton1)
        val restoreButton = view.findViewById<Button>(R.id.restoreButton1)
        view.findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(requireContext(), Profile::class.java))
        }
        backupButton.setOnClickListener {
            openFileCreator()
        }

        restoreButton.setOnClickListener {
            openFilePicker()
        }

        return view
    }

    private fun openFileCreator() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "FinanceBackup_$timestamp.json")
        }
        createFileLauncher.launch(intent)
    }

    private fun backupData(uri: android.net.Uri) {
        try {
            // Gather data from SharedPreferences
            val income = sharedPreferences.getFloat("income", 0f)
            val expenses = sharedPreferences.getFloat("expenses", 0f)
            val savings = sharedPreferences.getFloat("savings", 0f)
            val budgetPercentage = sharedPreferences.getFloat("budget_percentage", 50f)
            val transactions = sharedPreferences.getString("transactions", "") ?: ""
            val transactionDetails = sharedPreferences.getString("transaction_details", "") ?: ""

            // Log the data to verify it
            Log.d(TAG, "Backup Data - Income: $income, Expenses: $expenses, Savings: $savings")
            Log.d(TAG, "Budget Percentage: $budgetPercentage")
            Log.d(TAG, "Transactions: $transactions")
            Log.d(TAG, "Transaction Details: $transactionDetails")

            // Create JSON object
            val json = JSONObject().apply {
                put("income", income)
                put("expenses", expenses)
                put("savings", savings)
                put("budget_percentage", budgetPercentage)
                put("transactions", transactions)
                put("transaction_details", transactionDetails)
            }

            // Log the JSON string to verify
            val jsonString = json.toString(2)
            Log.d(TAG, "JSON to write: $jsonString")

            // Write JSON to the selected URI
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                throw Exception("Failed to open output stream for URI")
            }

            outputStream.use { output ->
                output.write(jsonString.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        pickFileLauncher.launch(intent)
    }

    private fun restoreData(uri: android.net.Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val data = inputStream?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Failed to read file")

            // Log the file content to verify
            Log.d(TAG, "Restore File Content: $data")

            // Parse JSON
            val json = JSONObject(data)

            // Extract data with defaults
            val income = json.optDouble("income", 0.0).toFloat()
            val expenses = json.optDouble("expenses", 0.0).toFloat()
            val savings = json.optDouble("savings", 0.0).toFloat()
            val budgetPercentage = json.optDouble("budget_percentage", 50.0).toFloat()
            val transactions = json.optString("transactions", "")
            val transactionDetails = json.optString("transaction_details", "")

            // Log the parsed data
            Log.d(TAG, "Restored - Income: $income, Expenses: $expenses, Savings: $savings")
            Log.d(TAG, "Restored - Budget Percentage: $budgetPercentage")
            Log.d(TAG, "Restored - Transactions: $transactions")
            Log.d(TAG, "Restored - Transaction Details: $transactionDetails")

            // Validate required fields
            if (transactions.isEmpty() && transactionDetails.isEmpty()) {
                throw Exception("Invalid backup file: Missing transaction data")
            }

            // Restore to SharedPreferences
            editor.putFloat("income", income)
            editor.putFloat("expenses", expenses)
            editor.putFloat("savings", savings)
            editor.putFloat("budget_percentage", budgetPercentage)
            editor.putString("transactions", transactions)
            editor.putString("transaction_details", transactionDetails)
            editor.apply()

            Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}