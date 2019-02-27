package org.reindexer.cproto

import org.reindexer.Consts
import org.reindexer.cjson.ByteArraySerializer
import org.reindexer.cjson.Serializer
import org.reindexer.exceptions.InvalidProtocolException
import org.reindexer.exceptions.ReindexerException
import org.reindexer.query.NewError

import java.util.ArrayList

class RPCDecoder(private val ser: Serializer) {

    private val args = ArrayList<Any>()


    fun decode(): List<Any> {

        val err = errCode()
        if (err != null) {
            //args.add(err);
            //return args;
            throw ReindexerException(err.message) // TODO code
        }

        val retCount = argsCount()
        if (retCount > 0) {
            for (i in 0 until retCount) {
                args.add(intfArg())
            }
        }
        return args
    }

    private fun intfArg(): Any {
        val type = ser.varUInt.toInt()
        when (type) {
            Consts.ValueInt -> return Integer.valueOf(ser.varInt.toInt())
            Consts.ValueBool -> return java.lang.Boolean.valueOf(ser.varInt != 0L)
            Consts.ValueString -> return ser.vString
            Consts.ValueInt64 -> return ser.varInt
            Consts.ValueDouble -> return ser.double
            else -> throw InvalidProtocolException(String.format("cproto: Unexpected arg type %d", type))
        }
    }

    private fun argsCount(): Int {
        return ser.varUInt.toInt()
    }

    private fun errCode(): NewError? {
        val code = ser.varUInt.toInt()
        val message = ser.vString
        return if (code != 0) {
            object : NewError {
                override val code: Int
                    get() = code

                override val message: String
                    get() = message
            }
        } else {
            null
        }
    }

    override fun toString(): String {
        var res = ""
        for (o in args) {
            if (o is NewError) {
                res = res + o.code + " " + o.message
            } else if (o is String) {
                res = "$res$o "
            } else if (o is Long) {
                res = res + o.toLong() + " "
            } else {
                res = res + o.javaClass.getCanonicalName() + " "
            }
        }
        return res
    }

    companion object {

        fun newRPCDecoder(buf: ByteArray): RPCDecoder {
            return RPCDecoder(ByteArraySerializer.getSerializer(buf))
        }
    }


}
