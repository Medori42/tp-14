package com.medori42.soapclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.medori42.soapclient.R
import com.medori42.soapclient.model.Account
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying a list of accounts.
 *
 * Handles the binding of [Account] data to the UI views.
 * @author Medori42
 */
class AccountAdapter : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
    private var accounts = mutableListOf<Account>()

    var onEditClick: ((Account) -> Unit)? = null
    var onDeleteClick: ((Account) -> Unit)? = null

    /**
     * Updates the list of accounts displayed.
     *
     * @param newAccounts The new list of accounts.
     */
    fun updateAccounts(newAccounts: List<Account>) {
        accounts.clear()
        accounts.addAll(newAccounts)
        notifyDataSetChanged()
    }

    /**
     * Removes an account from the list.
     *
     * @param account The account to remove.
     */
    fun removeAccount(account: Account) {
        val position = accounts.indexOf(account)
        if (position >= 0) {
            accounts.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    /**
     * ViewHolder class for account items.
     */
    inner class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val id: TextView = view.findViewById(R.id.textId)
        private val balance: TextView = view.findViewById(R.id.textSolde)
        private val type: Chip = view.findViewById(R.id.textType)
        private val creationDate: TextView = view.findViewById(R.id.textDate)
        private val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)

        fun bind(account: Account) {
            id.text = "Account Number ${account.id}"
            balance.text = "${account.balance} DH"
            type.text = account.type.name
            creationDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(account.creationDate)

            btnEdit.setOnClickListener { onEditClick?.invoke(account) }
            btnDelete.setOnClickListener { onDeleteClick?.invoke(account) }
        }
    }
}