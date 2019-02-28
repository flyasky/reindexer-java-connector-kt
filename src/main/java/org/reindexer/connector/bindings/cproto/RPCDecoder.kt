package org.reindexer.connector.bindings.cproto

import org.reindexer.connector.Consts
import org.reindexer.connector.Err
import org.reindexer.connector.cjson.ByteArraySerializer
import org.reindexer.connector.cjson.Serializer
import org.reindexer.connector.exceptions.InvalidProtocolException

class RPCDecoder(private val ser: Serializer) {

    fun intfArg(): Any {
        val type = ser.getVarUInt().toInt()
        return when (type) {
            Consts.ValueInt -> Integer.valueOf(ser.getVarInt().toInt())
            Consts.ValueBool -> java.lang.Boolean.valueOf(ser.getVarInt() != 0L)
            Consts.ValueString -> ser.getVString()
            Consts.ValueInt64 -> ser.getVarInt()
            Consts.ValueDouble -> ser.getDouble()
            else -> throw InvalidProtocolException(String.format("cproto: Unexpected arg type %d", type))
        }
    }

    fun argsCount(): Int {
        return ser.getVarUInt().toInt()
    }

    fun errCode(): Err? {
        val code = ser.getVarUInt().toInt()
        val message = ser.getVString()
        return if (code != 0) {
            Err.newError(message, code)
        } else {
            null
        }
    }

    companion object {

        fun newRPCDecoder(buf: ByteArray): RPCDecoder {
            return RPCDecoder(ByteArraySerializer.getSerializer(buf))
        }
    }


}
