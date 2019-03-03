package org.reindexer.connector.cjson

import java.nio.ByteBuffer

interface Serializer {

    fun getUInt16(): Int

    fun getUInt32(): UInt

    fun getVarUInt(): Long

    fun getVarInt(): Long

    fun getVString(): String

    fun getVBytes(): String

    fun getDouble(): Double

    fun bytes(): ByteArray

    fun readIntBits(sz: Int): Long?

    fun putUInt16(v: Int): Serializer

    fun putUInt32(v: Long): Serializer

    fun writeIntBits(v: Long, sz: Int)

    fun putVarUInt(v: Long): Serializer

    fun putVarInt(a: Long): Serializer

    fun putVarCUInt(v: Int): Serializer

    fun putVString(a: String): Serializer

    fun putVBytes(a: ByteBuffer): Serializer

    fun write(a: ByteBuffer): Serializer

    fun putVBytes(a: ByteArray): Serializer
}
