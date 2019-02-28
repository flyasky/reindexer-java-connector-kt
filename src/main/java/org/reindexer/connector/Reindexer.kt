package org.reindexer.connector

import com.google.gson.GsonBuilder
import org.reindexer.connector.cjson.ByteArraySerializer
import org.reindexer.connector.cjson.Serializer
import org.reindexer.connector.def.IndexDef
import org.reindexer.connector.bindings.cproto.Cproto
import org.reindexer.connector.exceptions.NsExistsException
import org.reindexer.connector.exceptions.NsNotFoundException
import org.reindexer.connector.exceptions.ReindexerException
import org.reindexer.connector.exceptions.UnimplementedException
import org.reindexer.connector.options.NamespaceOptions

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Reindexer API
 */
class Reindexer {

    private var url: String
    private var binding: Binding
    private val namespaceMap: MutableMap<String, Namespace> = ConcurrentHashMap()

    private constructor(url: String, binding: Binding) {
        this.url = url
        this.binding = binding
        init()
    }

    private fun init() {
        val err = binding.init(url)
        if (err.code != 0) {
            throw ReindexerException(err.message)
        }
    }

    fun openNamespace(namespace: String, options: NamespaceOptions, s: Class<*>) {
        registerNamespace(namespace, options, s)
        val ns = getNs(namespace)
        val res = binding.openNamespace(ns.name, options.enableStorage(), options.dropOnFileFormatError())
        if (!res.isOk()) {
            throw ReindexerException(res.message)
        }

/*
        for (indexDef in ns.indexes) {
            binding.addIndex(namespace, indexDef)
        }
*/

        /*
        for (int retry = 0; retry < 2; retry++) {
            if (err = db.binding.OpenNamespace(namespace, opts.enableStorage, opts.dropOnFileFormatError); err != nil {
                break
            }

            for _, indexDef := range ns.indexes {
                if err = db.binding.AddIndex(namespace, indexDef); err != nil {
                    break
                }
            }

            if err != nil {
                rerr, ok := err.(bindings.Err)
                if ok && rerr.Code() == bindings.ErrConflict && opts.dropOnIndexesConflict {
                    db.binding.DropNamespace(namespace)
                    continue
                }
                db.binding.CloseNamespace(namespace)
                break
            }

            break
        }
        */
    }

    /**
     * Upsert (Insert or Update) item to index.
     * Item must be the same type as item passed to OpenNamespace, or []byte with json
     *
     * @param namespace
     * @param item
     * @param <T>
    </T> */
    fun <T: Any> upsert(namespace: String, item: T, vararg precepts: String) {
        val ns = getNs(namespace.toLowerCase())
        modifyItem(ns, item, null, Consts.ModeUpsert, arrayOf(*precepts))
    }

    private fun getNs(namespace: String): Namespace {
        val name = namespace.toLowerCase()
        return namespaceMap[name] ?: throw NsNotFoundException()
    }

    /**
     * There are no data and indexes changes will be performed
     *
     * @param namespace
     * @param options
     * @param clazz
     */
    private fun registerNamespace(namespace: String, options: NamespaceOptions, clazz: Class<*>) {

        val name = namespace.toLowerCase()
        val ns = Namespace(name, clazz)
        if (ns == namespaceMap[name]) {
            throw NsExistsException()
        }

        /*
        ns := &reindexerNamespace{
            cacheItems:    make(map[int]cacheItem, 100),
            rtype:         t,
            name:          namespace,
            joined:        make(map[string][]int),
            opts:          *opts,
            cjsonState:    cjson.NewState(),
            deepCopyIface: haveDeepCopy,
            nsHash:        db.nsHashCounter,
            opened:        false,
        }
        */
        ns.options = options
        ns.indexes = parseIndex(name, clazz, ns.joined)
        namespaceMap[name] = ns
    }

    // TODO
    private fun parseIndex(name: String, clazz: Class<*>, joined: Map<String, IntArray>): List<IndexDef> {
        return ArrayList()
    }

    internal inner class PackItemResult {
        var format = 0
        var stateToken = 0
    }

    private fun <T : Any> packItem(ns: Namespace, item: T, json: ByteBuffer?, ser: Serializer): PackItemResult {

        if (ns.clazz != item.javaClass) {
            throw IllegalArgumentException() // TODO ErrWrongType
        }

        val res = PackItemResult()

        var sJson: String? = null
        if (item != null) {
            // json, _ = item.([]byte)
            val gson = GsonBuilder().create()
            sJson = gson.toJson(item)
        }

        if (sJson == null) {
            throw UnimplementedException()
            /*t := reflect.TypeOf(item)
            if t.Kind() == reflect.Ptr {
                t = t.Elem()
            }
            if ns.rtype.Name() != t.Name() {
                panic(ErrWrongType)
            }

            format = bindings.FormatCJson

            enc := ns.cjsonState.NewEncoder()
            if stateToken, err = enc.Encode(item, ser); err != nil {
                return
            }*/
        } else {
            res.format = Consts.FormatJson
            ser.write(ByteBuffer.wrap(sJson.toByteArray()))
        }
        return res
    }

    /**
     * Returns number of query results rawQueryParams.count
     */
    private fun <T : Any> modifyItem(ns: Namespace, item: T, json: ByteBuffer?, mode: Int, percepts: Array<String>) : Int {

        val ser = ByteArraySerializer.newSerializer()  //cjson.NewPoolSerializer()
        val packedItem = packItem(ns, item, json, ser)

        val res = binding.modifyItem(ns.hashCode(), ns.name, packedItem.format, ser.bytes(), mode, percepts, packedItem.stateToken)

        if (!res.error.isOk()) {
            if (res.error.code == Consts.ErrStateInvalidated) {
                //Query(ns.name).Limit(0).Exec().Close()
            }
            throw ReindexerException(res.error.message)
        }

        return 0

        /*TODO

            for tryCount := 0; tryCount < 2; tryCount++ {
                ser := cjson.NewPoolSerializer()
                defer ser.Close()

                format := 0
                stateToken := 0

                if format, stateToken, err = packItem(ns, item, json, ser); err != nil {
                    return
                }

                out, err := db.binding.ModifyItem(ns.nsHash, ns.name, format, ser.Bytes(), mode, precepts, stateToken)

                if err != nil {
                    rerr, ok := err.(bindings.Err)
                    if ok && rerr.Code() == bindings.ErrStateInvalidated {
                        db.Query(ns.name).Limit(0).Exec().Close()
                        err = rerr
                        continue
                    }
                    return 0, err
                }

                defer out.Free()

                rdSer := newSerializer(out.GetBuf())
                rawQueryParams := rdSer.readRawQueryParams(func(nsid int) {
                    ns.cjsonState.ReadPayloadType(&rdSer.Serializer)
                })

                if rawQueryParams.count == 0 {
                    return 0, err
                }

                resultp := rdSer.readRawtItemParams()

                ns.cacheLock.Lock()
                delete(ns.cacheItems, resultp.id)
                ns.cacheLock.Unlock()
                return rawQueryParams.count, err
            }
            return 0, err
        }

         */
    }

    companion object {
        @JvmStatic
        fun newReindexer(url: String): Reindexer {
            val protocol = url.substring(0, url.indexOf(":"))
            when (protocol) {
                "cproto" -> return Reindexer(url, Cproto())
                "http" ->
                    //return new Reindexer(url, new RestApiBinding());
                    throw UnimplementedException()
                "builtin" ->
                    //return new Reindexer(url, new BuiltinBinding());
                    throw UnimplementedException()
                "builtinserver" -> throw UnimplementedException()
                else -> throw IllegalArgumentException()
            }
        }

    }
}

