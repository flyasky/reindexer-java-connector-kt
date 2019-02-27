package org.reindexer.cproto

import org.reindexer.cjson.ByteArraySerializer
import org.reindexer.connector.RawBuffer
import org.reindexer.exceptions.NetworkException
import org.reindexer.exceptions.InvalidProtocolException
import org.reindexer.utils.Utils

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class Connection {
    // TODO
    private val lastReadStamp: Int = 0

    private var clientSocket: Socket
    private var outToServer: DataOutputStream
    private var inFromServer: DataInputStream

    constructor(host: String, port: Int) {
        try {
            clientSocket = Socket(host, port)
            outToServer = DataOutputStream(clientSocket.getOutputStream())
            inFromServer = DataInputStream(clientSocket.getInputStream())
        } catch (e: IOException) {
            throw NetworkException(e)
        }

    }

    // TODO
    @Throws(Exception::class)
    fun close() {
        clientSocket.close()
    }

    fun rpcCall(cmd: Int, vararg args: Any): RawBuffer {

        // FIXME return !=0 on Exception

        val `in` = RPCEncoder.newRPCEncoder(cmd, seq)
        `in`.putAllArgs(*args)
        val result: ByteArray
        try {
            outToServer.write(`in`.bytes())
            result = readReply(inFromServer)
        } catch (e: IOException) {
            throw NetworkException(e)
        }

        val out = RPCDecoder.newRPCDecoder(result)
        out.decode()
        println("FROM SERVER: " + Utils.bytesToHex(result))
        println("RPCDecoder: $out")

        seq++  // FIXME
        return NetBuffer(result)
    }

    companion object {

        internal val cprotoMagic = 0xEEDD1132L
        internal val cprotoVersion = 0x101
        internal val cprotoHdrLen = 16

        // TODO
        private val queueSize = 40
        // TODO
        private var seq = 0

        fun readReply(din: DataInputStream): ByteArray {
            val header = ByteArray(cprotoHdrLen)

            try {
                din.readFully(header)

                val ser = ByteArraySerializer.getSerializer(header)
                val magic = ser.getUInt32()
                val version = ser.getUInt16()
                /**/
                ser.getUInt16()
                val size = ser.getUInt32().toInt()
                val rseq = ser.getUInt32()
                if (magic != cprotoMagic.toUInt()) {
                    throw InvalidProtocolException(String.format("Invalid cproto magic '%08X'", magic))
                }

                if (version < cprotoVersion) {
                    throw InvalidProtocolException(String.format("Unsupported cproto version '%04X'. " +
                            "This client expects reindexer server v1.9.8+", version))
                }

                val answer = ByteArray(size)
                din.readFully(answer)

                return answer
            } catch (e: IOException) {
                throw NetworkException(e)
            }

        }
    }

}
