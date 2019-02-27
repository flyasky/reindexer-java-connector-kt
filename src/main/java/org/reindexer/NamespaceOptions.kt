package org.reindexer

interface NamespaceOptions {
    fun enableStorage(): Boolean
    fun dropOnFileFormatError(): Boolean
}
