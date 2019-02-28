package org.reindexer.connector.options

interface NamespaceOptions {
    fun enableStorage(): Boolean
    fun dropOnFileFormatError(): Boolean
}
