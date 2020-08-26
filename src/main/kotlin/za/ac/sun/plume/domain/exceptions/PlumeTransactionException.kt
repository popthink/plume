package za.ac.sun.plume.domain.exceptions

/**
 * Thrown to indicate a transaction related failure when attempting to create or commit a graph database transaction.
 */
class PlumeTransactionException(message: String) : Exception(message)