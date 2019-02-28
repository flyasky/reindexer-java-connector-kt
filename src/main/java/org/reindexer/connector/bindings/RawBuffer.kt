package org.reindexer.connector.bindings

interface RawBuffer {
    fun buf(): ByteArray
    fun free()
}
