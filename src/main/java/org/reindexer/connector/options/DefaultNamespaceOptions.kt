package org.reindexer.connector.options

class DefaultNamespaceOptions : NamespaceOptions {
    override fun enableStorage(): Boolean {
        return false
    }

    override fun dropOnFileFormatError(): Boolean {
        return true
    }
}
