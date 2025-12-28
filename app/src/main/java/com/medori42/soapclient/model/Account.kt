package com.medori42.soapclient.model

import java.util.Date

/**
 * Represents a bank account.
 *
 * @property id Unique identifier of the account.
 * @property balance Current balance of the account.
 * @property creationDate Date when the account was created.
 * @property type Type of the account (CHECKING or SAVINGS).
 * @author Medori42
 */
data class Account(
    val id: Long?,
    val balance: Double,
    val creationDate: Date,
    val type: AccountType
)

/**
 * Enumeration of account types.
 */
enum class AccountType {
    CHECKING, // Corresponds to COURANT
    SAVINGS   // Corresponds to EPARGNE
}