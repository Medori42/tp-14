package com.medori42.soapclient

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.medori42.soapclient.adapter.AccountAdapter
import com.medori42.soapclient.model.AccountType
import com.medori42.soapclient.network.SoapService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Activity for the Soap Client application.
 *
 * Displays a list of accounts and allows adding/deleting accounts via SOAP service.
 * @author Medori42
 */
class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAdd: Button
    private val adapter = AccountAdapter()
    private val soapService = SoapService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadAccounts()
    }

    /**
     * Initializes the views.
     */
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        btnAdd = findViewById(R.id.fabAdd)
    }

    /**
     * Configures the RecyclerView.
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onDeleteClick = { account ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        // Safe call for ID
                        val id = account.id ?: return@launch
                        val success = soapService.deleteAccount(id)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                adapter.removeAccount(account)
                                Toast.makeText(this@MainActivity, "Account deleted.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Error during deletion.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        // Edit functionality not fully implemented in original code, so keeping as placeholder or implementing basic
        adapter.onEditClick = {
            Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configures the listeners.
     */
    private fun setupListeners() {
        btnAdd.setOnClickListener { showAddAccountDialog() }
    }

    /**
     * Shows the dialog to add a new account.
     */
    private fun showAddAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup, null)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("New Account")
            .setPositiveButton("Add") { _, _ ->
                val etBalance = dialogView.findViewById<TextInputEditText>(R.id.etSolde)
                val radioChecking = dialogView.findViewById<RadioButton>(R.id.radioCourant)

                val balance = etBalance.text.toString().toDoubleOrNull() ?: 0.0
                val type = if (radioChecking.isChecked) AccountType.CHECKING else AccountType.SAVINGS

                lifecycleScope.launch(Dispatchers.IO) {
                    val success = soapService.createAccount(balance, type)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@MainActivity, "Account added.", Toast.LENGTH_SHORT).show()
                            loadAccounts()
                        } else {
                            Toast.makeText(this@MainActivity, "Error adding account.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Loads the list of accounts from the SOAP service.
     */
    private fun loadAccounts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val accounts = soapService.getAccounts()
                withContext(Dispatchers.Main) {
                    if (accounts.isNotEmpty()) {
                        adapter.updateAccounts(accounts)
                    } else {
                        Toast.makeText(this@MainActivity, "No accounts found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}