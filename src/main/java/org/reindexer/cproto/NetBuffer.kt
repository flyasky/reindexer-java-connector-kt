package org.reindexer.cproto

import org.reindexer.connector.RawBuffer

class NetBuffer(val res: ByteArray) : RawBuffer {

    override fun buf(): ByteArray {
        return res
    }

    override fun free() {

    }
}
