package com.example.labexam3

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AllTransactionsActivity : AppCompatActivity() {
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val REQUEST_CODE_STORAGE_PERMISSION = 1002
    private val REQUEST_CODE_RESTORE_FILE = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)

        transactionsContainer = findViewById(R.id.transactionsInnerContainer)
        sharedPreferences = getSharedPreferences("financeData", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Initialize Backup and Restore buttons
//        findViewById<Button>(R.id.backupButton).setOnClickListener {
//            backupData()
//        }
//        findViewById<Button>(R.id.restoreButton).setOnClickListener {
//            restoreData()
//        }

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                backupData()
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot backup data.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESTORE_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonString = inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(jsonString)
                        editor.putString("transactions", json.optString("transactions", ""))
                        editor.putString("transaction_details", json.optString("transaction_details", ""))
                        editor.putFloat("income", json.optDouble("income", 0.0).toFloat())
                        editor.putFloat("expenses", json.optDouble("expenses", 0.0).toFloat())
                        editor.putFloat("savings", json.optDouble("savings", 0.0).toFloat())
                        editor.putFloat("budget_percentage", json.optDouble("budget_percentage", 50.0).toFloat())
                        editor.apply()
                        loadTransactions()
                        Toast.makeText(this, "Data restored successfully.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("AllTransactionsActivity", "Error restoring data: ${e.message}", e)
                    Toast.makeText(this, "Failed to restore data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun backupData() {
        try {
            // Check storage permission for Android 9 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
                return
            }

            // Create JSON object with all SharedPreferences data
            val json = JSONObject().apply {
                put("transactions", sharedPreferences.getString("transactions", ""))
                put("transaction_details", sharedPreferences.getString("transaction_details", ""))
                put("income", sharedPreferences.getFloat("income", 0f))
                put("expenses", sharedPreferences.getFloat("expenses", 0f))
                put("savings", sharedPreferences.getFloat("savings", 0f))
                put("budget_percentage", sharedPreferences.getFloat("budget_percentage", 50f))
            }

            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "FinanceBackup_$timestamp.json"

            // Save to Documents directory
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val backupFile = File(documentsDir, fileName)
            FileOutputStream(backupFile).use { outputStream ->
                outputStream.write(json.toString().toByteArray())
            }

            Toast.makeText(this, "Backup saved to Documents/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("AllTransactionsActivity", "Error backing up data: ${e.message}", e)
            Toast.makeText(this, "Failed to backup data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreData() {
        try {
            // Check storage permission for Android 9 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
                return
            }

            // Open file picker for JSON files
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/json"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Select Backup File"), REQUEST_CODE_RESTORE_FILE)
        } catch (e: Exception) {
            Log.e("AllTransactionsActivity", "Error initiating restore: ${e.message}", e)
            Toast.makeText(this, "Failed to initiate restore.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransactions() {
        val transactions = sharedPreferences.getString("transactions", "") ?: ""
        val transactionDetails = sharedPreferences.getString("transaction_details", "") ?: ""
        Log.d("TRANSACTION_DEBUG", "Stored transactions: $transactions")
        Log.d("TRANSACTION_DEBUG", "Stored transaction details: $transactionDetails")

        val transactionList = transactions.split("\n").filter { it.isNotBlank() }
        val transactionDetailsList = transactionDetails.split("\n").filter { it.isNotBlank() }

        transactionsContainer.removeAllViews()

        if (transactionList.isEmpty()) {
            val placeholder = TextView(this).apply {
                text = "No transactions available"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, 16, 0, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }
            transactionsContainer.addView(placeholder)
            return
        }

        transactionList.forEach { transaction ->
            val parts = transaction.split(": ", " - ")
            val type = parts.getOrNull(0) ?: "Unknown"
            val amountString = parts.getOrNull(1) ?: "Rs. 0"
            val description = parts.getOrNull(2) ?: "No description"

            // Extract amount without "Rs. " for matching
            val amount = amountString.replace("Rs. ", "").toFloatOrNull() ?: 0f

            // Find matching transaction details
            val matchingDetail = transactionDetailsList.find { detail ->
                val detailParts = detail.split(",")
                val detailType = detailParts.getOrNull(0)
                val detailAmount = detailParts.getOrNull(3)?.toFloatOrNull() ?: 0f
                val detailDescription = detailParts.getOrNull(4) ?: ""
                detailType == type && detailAmount == amount && detailDescription == description
            }

            val date = matchingDetail?.split(",")?.getOrNull(1) ?: "N/A"
            val category = matchingDetail?.split(",")?.getOrNull(2) ?: "Uncategorized"

            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(32, 16, 32, 16)
                }
                radius = 16f
                cardElevation = 10f
                setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            }

            val innerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 40, 40, 40)
            }

            val typeView = TextView(this).apply {
                text = "🏷 Type: $type"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
            }

            val dateView = TextView(this).apply {
                text = "📅 Date: $date"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, 8, 0, 0)
            }

            val categoryView = TextView(this).apply {
                text = "🗂 Category: $category"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, 8, 0, 0)
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
                ).apply {
                    topMargin = 16
                    bottomMargin = 16
                }
                setBackgroundColor(0x22000000) // Semi-transparent divider
            }

            val amountView = TextView(this).apply {
                text = "💰 Amount: $amountString"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }

            val descriptionView = TextView(this).apply {
                text = "📋 Description: $description"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, 8, 0, 0)
            }


            val buttonContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
                gravity = android.view.Gravity.END
            }

            val editButton = ImageButton(this).apply {
                setImageResource(R.drawable.ic_edit)
                background = null
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
                setOnClickListener {
                    val intent = Intent(context, EditTransactionActivity::class.java)
                    intent.putExtra("transaction", transaction)
                    intent.putExtra("date", date)
                    intent.putExtra("category", category)
                    startActivity(intent)
                }
            }

            val deleteButton = ImageButton(this).apply {
                setImageResource(R.drawable.ic_delete)
                background = null
                setOnClickListener {
                    deleteTransaction(transaction)
                }
            }

            buttonContainer.addView(editButton)
            buttonContainer.addView(deleteButton)

            innerLayout.addView(typeView)
            innerLayout.addView(dateView)
            innerLayout.addView(categoryView)
            innerLayout.addView(divider)
            innerLayout.addView(amountView)
            innerLayout.addView(descriptionView)
            innerLayout.addView(buttonContainer)
            card.addView(innerLayout)

            transactionsContainer.addView(card)
        }
    }

    private fun deleteTransaction(transactionToDelete: String) {

        val parts = transactionToDelete.split(": ", " - ")
        val type = parts.getOrNull(0) ?: "Unknown"
        val amountString = parts.getOrNull(1)?.replace("Rs. ", "") ?: "0"
        val amount = amountString.toFloatOrNull() ?: 0f


        var income = sharedPreferences.getFloat("income", 0f)
        var expenses = sharedPreferences.getFloat("expenses", 0f)
        var savings = sharedPreferences.getFloat("savings", 0f)

        when (type.lowercase()) {
            "income" -> income -= amount
            "expense" -> expenses -= amount
            "saving" -> savings -= amount
        }


        var transactions = sharedPreferences.getString("transactions", "") ?: ""
        transactions = transactions.split("\n")
            .filter { it.isNotBlank() && it != transactionToDelete }
            .joinToString("\n") { it }


        var transactionDetails = sharedPreferences.getString("transaction_details", "") ?: ""
        val transactionDetailsList = transactionDetails.split("\n").filter { it.isNotBlank() }
        val matchingDetail = transactionDetailsList.find { detail ->
            val detailParts = detail.split(",")
            val detailType = detailParts.getOrNull(0)
            val detailAmount = detailParts.getOrNull(3)?.toFloatOrNull() ?: 0f
            val detailDescription = detailParts.getOrNull(4) ?: ""
            detailType == type && detailAmount == amount && detailDescription == (parts.getOrNull(2) ?: "")
        }
        transactionDetails = transactionDetailsList
            .filter { it != matchingDetail }
            .joinToString("\n") { it }


        editor.putFloat("income", income.coerceAtLeast(0f))
        editor.putFloat("expenses", expenses.coerceAtLeast(0f))
        editor.putFloat("savings", savings.coerceAtLeast(0f))
        editor.putString("transactions", transactions)
        editor.putString("transaction_details", transactionDetails)
        editor.apply()


        loadTransactions()
    }
}