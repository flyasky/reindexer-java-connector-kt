package org.reindexer.cjson

import java.nio.ByteBuffer

interface Serializer {

    val uInt16: Int

    val uInt32: UInt

    val varUInt: Long

    val varInt: Long

    val vString: String

    val vBytes: String

    val double: Double

    fun bytes(): ByteArray

    fun readIntBits(sz: Int): Long?

    fun putUInt16(v: Int): Serializer

    fun putUInt32(v: Long): Serializer

    fun writeIntBits(v: Long, sz: Int)

    fun putVarUInt(v: Long): Serializer

    fun putVarInt(a: Long): Serializer

    fun putVString(a: String): Serializer

    fun putVBytes(a: ByteBuffer): Serializer

    fun write(a: ByteBuffer): Serializer
}
