package org.reindexer.cjson

import org.junit.Assert
import org.junit.Test
import org.reindexer.cproto.Connection
import org.reindexer.utils.Utils

class SerializerTest {

    @Test
    fun testUnsignedInt16() {
        val s = ByteArraySerializer.getSerializer(Utils.hexToBytes("ffff"))
        Assert.assertEquals(0xffff, s.uInt16.toLong())
    }

    @Test
    fun testUnsignedInt32() {
        val s = ByteArraySerializer.getSerializer(Utils.hexToBytes("ffffffff"))
        Assert.assertEquals(0xffffffffL.toUInt(), s.uInt32)
    }

    @Test
    fun testUnsignedInt32Order() {
        val s = ByteArraySerializer.getSerializer(Utils.hexToBytes("3211ddee"))
        Assert.assertEquals(0xEEDD1132.toUInt(), s.uInt32)
    }

}
