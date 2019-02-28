package org.reindexer.connector.bindings.cproto

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.reindexer.connector.bindings.Binding
import org.reindexer.connector.bindings.Err
import org.reindexer.connector.bindings.Res
import org.reindexer.connector.bindings.def.IndexDef
import org.reindexer.connector.bindings.def.NamespaceDef
import org.reindexer.connector.exceptions.UnimplementedException
import org.reindexer.connector.bindings.def.StorageOpts

import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class Cproto : Binding {

    private lateinit var c: Connection
    private var isLoggedIn = false

    internal enum class OperationType {
        READ, WRITE
    }

    override fun init(url: String, vararg options: Any): Err {
        val uri: URI
        try {
            uri = URI(url)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

        val host = uri.host
        val port = uri.port
        val database = uri.path.substring(1)
        val userInfo = uri.userInfo
        var user = ""
        var password = ""
        if (userInfo != null) {
            val userInfoArray = userInfo.split(":")
            if (userInfoArray.size == 2) {
                user = userInfoArray[0]
                password = userInfoArray[1]
            } else {
                throw IllegalArgumentException()
            }
        }
        c = Connection(host, port)
        var result = c.rpcCall(cmdLogin, user, password, database)
        if (result.error.code == 0) {
            isLoggedIn = true
            return Err.newOk()
        } else {
            return result.error
        }
    }

    override fun openNamespace(namespace: String, enableStorage: Boolean, dropOnFileFormatError: Boolean): Err {
        val storageOpts = StorageOpts(enableStorage, dropOnFileFormatError, true)
        val namespaceDef = NamespaceDef(storageOpts, namespace)
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
        val sNamespaceDef = gson.toJson(namespaceDef)
        // {"name":"items"} -> reindexer_tool -> {"name":"items","storage":{"enabled":true},"indexes":[]}
        //rpcCallNoResults(OperationType.WRITE, cmdOpenNamespace, sNamespaceDef); // also ok
        return rpcCallNoResults(OperationType.WRITE, cmdOpenNamespace, ByteBuffer.wrap(sNamespaceDef.toByteArray()))
    }

    override fun addIndex(namespace: String, indexDef: IndexDef): Err {
        TODO("not implemented")
    }

    override fun modifyItem(nsHash: Int, namespace: String, format: Int,
                            data: ByteArray, mode: Int, percepts: Array<String>, stateToken: Int): Res {
        val packedPercepts = ByteBuffer.allocate(0)
        if (percepts.isNotEmpty()) {
            throw UnimplementedException()
            /*
                ser1 := cjson.NewPoolSerializer()
                defer ser1.Close()

                ser1.PutVarCUInt(len(precepts))
                for _, precept := range precepts {
                    ser1.PutVString(precept)
                }
                packedPercepts = ser1.Bytes()
             */
        }
        return rpcCall(OperationType.WRITE, cmdModifyItem,
                    namespace, format, ByteBuffer.wrap(data), mode, packedPercepts, stateToken, 0)
    }

    override fun closeNamespace(namespace: String): Err {
        TODO("not implemented")
    }

    override fun dropNamespace(namespace: String): Err {
        TODO("not implemented")
    }

    override fun enableStorage(namespace: String): Err {
        TODO("not implemented")
    }

    override fun updateIndex(namespace: String, indexDef: IndexDef): Err {
        TODO("not implemented")
    }

    override fun dropIndex(namespace: String, index: String): Err {
        TODO("not implemented")
    }

    private fun rpcCallNoResults(op: OperationType, cmd: Int, vararg args: Any): Err {
        val res = rpcCall(op, cmd, *args)
        return res.error
    }

    private fun rpcCall(op: OperationType, cmd: Int, vararg args: Any): Res {
        return c.rpcCall(cmd, *args)
        /* TODO
        var attempts int
        switch op {
            case opRd:
                attempts = binding.retryAttempts.Read + 1
            default:
                attempts = binding.retryAttempts.Write + 1
        }
        for i := 0; i < attempts; i++ {
            if buf, err = binding.getConn().rpcCall(cmd, args...); err == nil {
                return
            }
            switch err.(type) {
            case net.Err, *net.OpError:
                time.Sleep(time.Second * time.Duration(i))
            default:
                return
            }
        }
        */
    }

    companion object {

        internal val cmdPing = 0
        internal val cmdLogin = 1
        internal val cmdOpenDatabase = 2
        internal val cmdCloseDatabase = 3
        internal val cmdDropDatabase = 4
        internal val cmdOpenNamespace = 16
        internal val cmdCloseNamespace = 17
        internal val cmdDropNamespace = 18
        internal val cmdAddIndex = 21
        internal val cmdEnumNamespaces = 22
        internal val cmdDropIndex = 24
        internal val cmdUpdateIndex = 25
        internal val cmdStartTransaction = 28 // FIXME
        internal val cmdAddTxItem = 29
        internal val cmdCommitTx = 30
        internal val cmdRollbackTx = 31
        internal val cmdCommit = 32
        internal val cmdModifyItem = 33
        internal val cmdDeleteQuery = 34
        internal val cmdSelect = 48
        internal val cmdSelectSQL = 49
        internal val cmdFetchResults = 50
        internal val cmdCloseResults = 51
        internal val cmdGetMeta = 64
        internal val cmdPutMeta = 65
        internal val cmdEnumMeta = 66
        internal val cmdCodeMax = 128

        internal val defConnPoolSize = 8
        internal val pingerTimeoutSec = 60
    }

}
