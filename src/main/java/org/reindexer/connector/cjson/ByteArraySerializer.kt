package org.reindexer.connector.cjson


import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Arrays

class ByteArraySerializer : Serializer {

    private var buf = ByteArray(0)
    private var pos: Int = 0

    private constructor()

    private constructor(buf: ByteArray) {
        this.buf = Arrays.copyOf(buf, buf.size)
    }

    override fun bytes(): ByteArray {
        return buf
    }

    /**
     * Читает без знака 2 байта (int16).
     *
     * В Java для беззнаковых надо использовать более широкий тип.
     */
    override fun getUInt16(): Int {
        return readIntBits(2).toInt()
    }

    override fun getUInt32(): UInt {
        return readIntBits(4).toUInt()
    }

    override fun getVarUInt(): Long {
        var x: Long = 0
        var s = 0
        var c = 0

        for (i in pos until buf.size) {
            c++
            // FIXME
            val b = (buf[i].toInt() and 0xFF).toLong()
            if (s >= 63) {
                if (s == 63 && b > 1 || s > 63)
                    throw NumberFormatException("Overflow: value is larger than 64 bits")
            }
            if (b and 0x80 == 0L) {
                pos = pos + c
                return x or (b shl s)
            }
            x = x or (b and 0x7F shl s)
            s += 7
        }
        throw IllegalArgumentException("Input buffer too small")
    }

    /**
     * https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java
     */
    override fun getVarInt(): Long {
        // This undoes the trick in writeSignedVarLong()
        // This extra step lets us deal with the largest signed values by treating
        // negative results from read unsigned methods as like unsigned values
        // Must re-flip the top bit if the original read value had it set.
        val raw = getVarUInt()
        val temp = raw shl 63 shr 63 xor raw shr 1
        return temp xor (raw and (1L shl 63))
    }

    override fun getVString(): String {
        val l = getVarUInt().toInt()
        if (pos + l > buf.size) {
            throw RuntimeException(String.format("Internal error: serializer need %d bytes, but only %d available",
                    l, buf.size - pos))
        }
        val v = String(Arrays.copyOfRange(buf, pos, pos + l), StandardCharsets.UTF_8)
        pos += l
        return v

    }

    // FIXME bytes?? encoding??
    override fun getVBytes(): String {
        val l = getUInt32().toInt()
        if (pos + l > buf.size) {
            throw RuntimeException(String.format("Internal error: serializer need %d bytes, but only %d available",
                    l, buf.size - pos))
        }
        val v = String(Arrays.copyOfRange(buf, pos, pos + l), StandardCharsets.UTF_8)
        pos += l
        return v
    }

    override fun getDouble(): Double {
        TODO("not implemented")
    }

    override fun readIntBits(sz: Int): Long {
        if (pos + sz > buf.size) {
            throw RuntimeException(String.format("Internal error: serializer need %d bytes, but only %d available",
                    pos + sz, buf.size - pos))
        }
        var v: Long = 0
        for (i in sz - 1 downTo 0) {
            v = ((buf[i + pos].toInt() and 0xFF) or (v shl 8).toInt()).toLong()
        }
        pos += sz
        return v
    }

    override fun putUInt16(v: Int): Serializer {
        writeIntBits(v.toLong(), java.lang.Short.BYTES)
        return this
    }

    override fun putUInt32(v: Long): Serializer {
        writeIntBits(v, Integer.BYTES)
        return this
    }

    override fun writeIntBits(v: Long, sz: Int) {
        var v = v
        val l = buf.size
        buf = Arrays.copyOf(buf, l + sz)
        for (i in 0 until sz) {
            buf[l + i] = v.toByte()
            v = v shr 8
        }
    }

    override fun putVarUInt(v: Long): Serializer {
        val l = buf.size
        val r = ByteArray(10)
        val rl = putUvarint(r, v)
        buf = Arrays.copyOf(buf, l + rl)
        for (i in 0 until rl) {
            buf[l + i] = r[i]
        }
        return this
    }

    // https://en.wikipedia.org/wiki/Variable-length_quantity
    private fun putUvarint(buf: ByteArray, x: Long): Int {
        var x = x
        var i = 0
        while (x >= 0x80) {
            buf[i] = (x or 0x80).toByte()
            x = x shr 7
            i++
        }
        buf[i] = x.toByte()
        return i + 1
    }

    override
            /**
             * https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java
             */
    fun putVarInt(a: Long): Serializer {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        putVarUInt(a shl 1 xor (a shr 31))
        return this
    }

    override fun putVString(a: String): Serializer {
        val sl = a.length
        putVarUInt(sl.toLong())

        val l = buf.size
        buf = Arrays.copyOf(buf, l + sl)

        for (i in 0 until sl) {
            buf[l + i] = a[i].toByte()
        }
        return this
    }

    override fun putVBytes(a: ByteBuffer): Serializer {
        val ar = a.array()
        val sl = ar.size
        putVarUInt(sl.toLong())
        val l = buf.size
        buf = Arrays.copyOf(buf, l + sl)
        for (i in 0 until sl) {
            buf[l + i] = ar[i]
        }
        return this
    }

    override fun putVBytes(a: ByteArray): Serializer {
        val ar = a
        val sl = ar.size
        putVarUInt(sl.toLong())
        val l = buf.size
        buf = Arrays.copyOf(buf, l + sl)
        for (i in 0 until sl) {
            buf[l + i] = ar[i]
        }
        return this
    }

    override fun write(a: ByteBuffer): Serializer {
        val ar = a.array()
        val sl = ar.size
        val l = buf.size
        buf = Arrays.copyOf(buf, l + sl)
        for (i in 0 until sl) {
            buf[l + i] = ar[i]
        }
        return this
    }

    companion object {

        fun getSerializer(buf: ByteArray): ByteArraySerializer {
            return ByteArraySerializer(buf)
        }

        fun newSerializer(): ByteArraySerializer {
            return ByteArraySerializer()
        }
    }
}
