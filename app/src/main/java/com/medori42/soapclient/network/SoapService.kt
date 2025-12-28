package com.medori42.soapclient.network

import com.medori42.soapclient.model.Account
import com.medori42.soapclient.model.AccountType
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.PropertyInfo
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service class for handling SOAP network requests.
 *
 * Interacts with the remote SOAP web service to manage accounts.
 * @author Medori42
 */
class SoapService {
    private val namespace = "http://ws.soapAcount/"
    private val url = "http://10.0.2.2:8082/services/ws"
    private val methodGetAccounts = "getComptes"
    private val methodCreateAccount = "createCompte"
    private val methodDeleteAccount = "deleteCompte"

    /**
     * Retrieves the list of accounts from the SOAP service.
     *
     * @return List of [Account] objects.
     */
    fun getAccounts(): List<Account> {
        val request = SoapObject(namespace, methodGetAccounts)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(url)
        val accounts = mutableListOf<Account>()

        try {
            transport.call("", envelope)
            // Handle cases where response might be null or single object vs vector
            if (envelope.bodyIn is SoapObject) {
                val response = envelope.bodyIn as SoapObject
                for (i in 0 until response.propertyCount) {
                    val soapAccount = response.getProperty(i) as SoapObject
                    val account = Account(
                        id = soapAccount.getPropertySafelyAsString("id")?.toLongOrNull(),
                        balance = soapAccount.getPropertySafelyAsString("solde")?.toDoubleOrNull() ?: 0.0,
                        creationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                            soapAccount.getPropertySafelyAsString("dateCreation")
                        ) ?: Date(),
                        type = try {
                            // Map 'COURANT'/'EPARGNE' from server to CHECKING/SAVINGS
                            val typeStr = soapAccount.getPropertySafelyAsString("type")
                            if (typeStr == "COURANT") AccountType.CHECKING else AccountType.SAVINGS
                        } catch (e: Exception) {
                            AccountType.CHECKING
                        }
                    )
                    accounts.add(account)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return accounts
    }

    /**
     * Creates a new account via the SOAP service.
     *
     * @param balance Initial balance of the account.
     * @param type Type of the account.
     * @return True if creation was successful, false otherwise.
     */
    fun createAccount(balance: Double, type: AccountType): Boolean {
        val request = SoapObject(namespace, methodCreateAccount)
        
        // Convert internal Enum to server expected string
        val serverTypeParam = if (type == AccountType.CHECKING) "COURANT" else "EPARGNE"

        request.addProperty("solde", balance.toString())
        request.addProperty("type", serverTypeParam)
        
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(url)

        return try {
            transport.call("", envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return True if deletion was successful, false otherwise.
     */
    fun deleteAccount(id: Long): Boolean {
        val request = SoapObject(namespace, methodDeleteAccount)
        
        val idProperty = PropertyInfo().apply {
            name = "id"
            value = id
            type = PropertyInfo.LONG_CLASS
        }
        request.addProperty(idProperty)
        
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(url)

        return try {
            transport.call("", envelope)
            // Usually returns boolean or void. If void, check if no exception.
            // Original code assumed boolean return.
            true 
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun SoapObject.getPropertySafelyAsString(name: String): String? {
        return if (hasProperty(name)) {
            val prop = getProperty(name)
            prop?.toString()
        } else {
            null
        }
    }
}