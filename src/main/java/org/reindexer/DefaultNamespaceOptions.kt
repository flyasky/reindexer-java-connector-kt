package org.reindexer

class DefaultNamespaceOptions : NamespaceOptions {
    override fun enableStorage(): Boolean {
        return false
    }

    override fun dropOnFileFormatError(): Boolean {
        return true
    }
}
