package org.reindexer.connector.bindings.cproto

import org.reindexer.connector.bindings.Err
import org.reindexer.connector.bindings.FetchMore
import org.reindexer.connector.bindings.RawBuffer
import java.util.ArrayList

class NetBuffer : RawBuffer, FetchMore {

    val buf: ByteArray
    private val args = ArrayList<Any>()

    constructor(buf: ByteArray) {
        this.buf = buf
    }

    override fun fetch(offset: Int, limit: Int, withItems: Boolean): Err {
        /*
            flags := 0
            if withItems {
                flags |= bindings.ResultsJson
            } else {
                flags |= bindings.ResultsCJson | bindings.ResultsWithItemID
            }
            //fmt.Printf("cmdFetchResults(reqId=%d, offset=%d, limit=%d, json=%v, flags=%v)\n", buf.reqID, offset, limit, withItems, flags)
            fetchBuf, err := buf.conn.rpcCall(cmdFetchResults, buf.reqID, flags, offset, limit)
            defer fetchBuf.Free()
            if err != nil {
                buf.close()
                return
            }
            fetchBuf.buf, buf.buf = buf.buf, fetchBuf.buf

            if err = buf.parseArgs(); err != nil {
                buf.close()
                return
            }
            buf.result = buf.args[0].([]byte)
            if buf.args[1].(int) == -1 {
                buf.closed = true
            }
            return

         */
        return Err.newError("OK", 0)
    }

    override fun buf(): ByteArray {
        return buf
    }

    fun parseArgs(): Err {
        args.clear()
        val dec = RPCDecoder.newRPCDecoder(buf)
        val err = dec.errCode()
        if (err != null) {
            return err
        }
        val retCount = dec.argsCount()
        if (retCount > 0) {
            for (i in 0..retCount-1) {
                args.add(dec.intfArg())
            }
        }
        return Err.newError("OK", 0)
    }

    override fun toString(): String {
        var res = ""
        for (o in args) {
            res = when (o) {
                is Err -> res + o.code + " " + o.message + " "
                is String -> "$res$o "
                is Long -> res + o.toLong() + " "
                else -> res + o.javaClass.getCanonicalName() + " "
            }
        }
        return res
    }

    override fun free() {
        if (buf != null) {
            close()
            //bufPool.Put(buf)
        }
    }

    private fun close() {
/*
        if (needClose && !closed) {
            closed = true

            //closeBuf = conn.rpcCall(cmdCloseResults, buf.reqID)

            if (err != null) {
                fmt.Printf("rx: query close error: %v", err)
            }
            closeBuf.Free()
        }
*/
    }

}
