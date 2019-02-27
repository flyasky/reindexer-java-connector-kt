package org.reindexer.cproto

import org.apache.commons.lang3.ArrayUtils
import org.reindexer.Consts
import org.reindexer.cjson.ByteArraySerializer
import org.reindexer.cjson.Serializer

import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.Collections

class RPCEncoder private constructor(private val cmd: Int, private val seq: Int) {
    private val args = ArrayList<Any>()

    fun bytes(): ByteArray {
        val body = ByteArraySerializer.newSerializer()
        body.putVarUInt(args.size.toLong())
        for (a in args) {
            if (a is Boolean) {
                body.putVarUInt(Consts.ValueBool.toLong())
                if (a) {
                    body.putVarUInt(1)
                } else {
                    body.putVarUInt(0)
                }
            } else if (a is Short) {
                body.putVarUInt(Consts.ValueInt.toLong())
                body.putVarInt((a as Int).toLong())
            } else if (a is Int) {
                body.putVarUInt(Consts.ValueInt.toLong())
                body.putVarInt(a.toLong())
            } else if (a is Long) {
                throw UnsupportedOperationException()
            } else if (a is String) {
                body.putVarUInt(Consts.ValueString.toLong())
                body.putVString(a)
            } else if (a is ByteBuffer) {
                body.putVarUInt(Consts.ValueString.toLong())
                body.putVBytes(a)
            }
            // TODO
            // case []byte:
            //     in.bytesArg(t)
            // case []int32:
            //     in.int32ArrArg(t)
        }
        val header = getHeader(body)
        return ArrayUtils.addAll(header.bytes(), *body.bytes())
    }

    private fun getHeader(body: Serializer): Serializer {
        val ser = ByteArraySerializer.newSerializer()
        ser.putUInt32(Connection.cprotoMagic)
        ser.putUInt16(Connection.cprotoVersion)
        ser.putUInt16(cmd)
        ser.putUInt32(body.bytes().size.toLong())
        ser.putUInt32(seq.toLong())
        return ser
    }

    fun putAllArgs(vararg a: Any): RPCEncoder {
        Collections.addAll(args, *a)
        return this
    }

    companion object {

        fun newRPCEncoder(cmd: Int, seq: Int): RPCEncoder {
            return RPCEncoder(cmd, seq)
        }
    }
}
