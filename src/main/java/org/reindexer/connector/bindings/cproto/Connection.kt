package org.reindexer.connector.bindings.cproto

import org.reindexer.connector.bindings.Res
import org.reindexer.connector.cjson.ByteArraySerializer
import org.reindexer.connector.exceptions.NetworkException
import org.reindexer.connector.exceptions.InvalidProtocolException

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

    fun rpcCall(cmd: Int, vararg args: Any): Res {

        // FIXME return !=0 on Exception

        val request = RPCEncoder.newRPCEncoder(cmd, seq)
        request.putAllArgs(*args)
        val result: ByteArray
        try {
            outToServer.write(request.bytes())
            result = readReply(inFromServer)
        } catch (e: IOException) {
            throw NetworkException(e)
        }

//        val response = RPCDecoder.newRPCDecoder(result)
//        response.decode()
//        println("FROM SERVER: " + Utils.bytesToHex(result))
//        //println("RPCDecoder: $response")

        seq++  // FIXME

        val r = NetBuffer(result)
        val err = r.parseArgs()
        return Res(err, r)
    }

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

    companion object {

        internal val cprotoMagic = 0xEEDD1132L
        internal val cprotoVersion = 0x101
        internal val cprotoHdrLen = 16

        // TODO
        private val queueSize = 40
        // TODO
        private var seq = 0
    }

}
