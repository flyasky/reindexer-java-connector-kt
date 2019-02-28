package org.reindexer.connector.options

data class NamespaceOptions(var enableStorage: Boolean,
                            var dropOnFileFormatError: Boolean,
                            var dropOnIndexesConflict: Boolean) {

    fun get() : NamespaceOptions {
        return this
    }

    /**
     * Only in memory namespace
     */
    fun setNoStorage() : NamespaceOptions {
        enableStorage = false
        return this
    }

    /**
     * Drop ns on index mismatch error
     */
    fun setDropOnIndexesConflict() : NamespaceOptions {
        dropOnIndexesConflict = true
        return this
    }

    /**
     * Drop on file errors
     */
    fun setDropOnFileFormatError() : NamespaceOptions {
        dropOnFileFormatError = true
        return this
    }

    companion object {
        @JvmStatic
        fun defaultOptions() : NamespaceOptions {
            return NamespaceOptions(true, false, false)
        }
    }
}
