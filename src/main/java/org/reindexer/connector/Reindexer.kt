package org.reindexer.connector

import com.google.gson.GsonBuilder
import org.reindexer.connector.bindings.Binding
import org.reindexer.connector.bindings.Consts
import org.reindexer.connector.bindings.Res
import org.reindexer.connector.cjson.ByteArraySerializer
import org.reindexer.connector.cjson.Serializer
import org.reindexer.connector.bindings.cproto.Cproto
import org.reindexer.connector.exceptions.NsExistsException
import org.reindexer.connector.exceptions.NsNotFoundException
import org.reindexer.connector.exceptions.ReindexerException
import org.reindexer.connector.exceptions.UnimplementedException
import org.reindexer.connector.options.NamespaceOptions
import org.reindexer.connector.query.Query
import org.reindexer.connector.query.iterator.Iterator
import org.reindexer.utils.Reflect
import org.slf4j.LoggerFactory
import java.lang.reflect.Type

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Reindex API
 */
class Reindexer {

    private val LOGGER = LoggerFactory.getLogger("org.reindexer.Reindexer")

    private var url: String
    private var binding: Binding
    private val namespaces: MutableMap<String, ReindexerNamespace> = ConcurrentHashMap()

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
        LOGGER.debug("OpenNamespace '$namespace'")
        registerNamespace(namespace, options, s)
        val ns = getNs(namespace)

        for (retry in 0..1) {
            var res = binding.openNamespace(ns.name, options.enableStorage, options.dropOnFileFormatError)
            if (!res.isOk()) {
                break
            }

            for (indexDef in ns.indexes) {
                res = binding.addIndex(ns.name, indexDef)
                LOGGER.debug("AddIndex '${indexDef.name}' result ${res.message}")
                if (!res.isOk()) {
                    LOGGER.debug("AddIndex error ${res.code} ${res.message}")
                    break
                }
            }

            if (!res.isOk()) {
                if (res.code == Consts.ErrConflict && options.dropOnIndexesConflict) {
                    LOGGER.debug("DropNamespace '$namespace'")
                    binding.dropNamespace(ns.name)
                    continue
                }
                LOGGER.debug("CloseNamespace '$namespace'")
                binding.closeNamespace(ns.name)
                break
            }
            break
        }
    }

    /**
     * Upsert (Insert or Update) item to index.
     * Item must be the same type as item passed to OpenNamespace, or []byte with json
     *
     * @param namespace
     * @param item
     * @param <T>
    </T> */
    fun upsert(namespace: String, item: Any, vararg precepts: String) {
        val ns = getNs(namespace.toLowerCase())
        modifyItem(ns, item, null, Consts.ModeUpsert, arrayOf(*precepts))
    }

    private fun getNs(namespace: String): ReindexerNamespace {
        val name = namespace.toLowerCase()
        return namespaces[name] ?: throw NsNotFoundException()
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
        val ns = ReindexerNamespace(name, clazz)
        if (ns == namespaces[name]) {
            throw NsExistsException()
        }
        /*
            copier, haveDeepCopy := reflect.New(t).Interface().(DeepCopy)
            if haveDeepCopy {
                cpy := copier.DeepCopy()
                cpyType := reflect.TypeOf(reflect.Indirect(reflect.ValueOf(cpy)).Interface())
                if cpyType != reflect.TypeOf(s) {
                    return ErrDeepCopyType
                }
            }
        */
        ns.opts = options
        ns.deepCopyIface = false // TODO
        ns.indexes = Reflect.parseIndex(name, clazz, ns.joined)
        namespaces[name] = ns
    }

    internal inner class PackItemResult {
        var format = 0
        var stateToken = 0
    }

    private fun packItem(ns: ReindexerNamespace, item: Any, json: ByteBuffer?, ser: Serializer): PackItemResult {

        if (ns.rtype != item.javaClass) {
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
    private fun modifyItem(ns: ReindexerNamespace, item: Any, json: ByteBuffer?, mode: Int, percepts: Array<String>) : Int {

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

    fun query(namespace: String): Query {
        return Query.newQuery(this, namespace)
    }

    /*private */fun execQuery(q: Query): Iterator? {
        /*result, err := db.prepareQuery(q, false)
        if err != nil {
            return errIterator(err)
        }
        iter := newIterator(q, result, q.nsArray, q.joinToFields, q.joinHandlers, q.context)
        return iter*/
        return null
    }

    private fun prepareQuery(q: Query, asJson: Boolean): Res {
/*
        if ns, err := db.getNS(q.Namespace); err == nil {
            q.nsArray = append(q.nsArray, nsArrayEntry{ns, ns.cjsonState.Copy()})
        } else {
            return nil, err
        }

        ser := q.ser
        for _, sq := range q.mergedQueries {
            if ns, err := db.getNS(sq.Namespace); err == nil {
            q.nsArray = append(q.nsArray, nsArrayEntry{ns, ns.cjsonState.Copy()})
        } else {
            return nil, err
        }
        }

        for _, sq := range q.joinQueries {
            if ns, err := db.getNS(sq.Namespace); err == nil {
            q.nsArray = append(q.nsArray, nsArrayEntry{ns, ns.cjsonState.Copy()})
        } else {
            return nil, err
        }
        }

        for _, mq := range q.mergedQueries {
            for _, sq := range mq.joinQueries {
            if ns, err := db.getNS(sq.Namespace); err == nil {
            q.nsArray = append(q.nsArray, nsArrayEntry{ns, ns.cjsonState.Copy()})
        } else {
            return nil, err
        }
        }
        }

        ser.PutVarCUInt(queryEnd)
        for _, sq := range q.joinQueries {
            ser.PutVarCUInt(sq.joinType)
            ser.Append(sq.ser)
            ser.PutVarCUInt(queryEnd)
        }

        for _, mq := range q.mergedQueries {
            ser.PutVarCUInt(merge)
            ser.Append(mq.ser)
            ser.PutVarCUInt(queryEnd)
            for _, sq := range mq.joinQueries {
            ser.PutVarCUInt(sq.joinType)
            ser.Append(sq.ser)
            ser.PutVarCUInt(queryEnd)
        }
        }

        for _, ns := range q.nsArray {
            q.ptVersions = append(q.ptVersions, ns.localCjsonState.Version^ns.localCjsonState.StateToken)
        }
        fetchCount := q.fetchCount
        if asJson {
            // json iterator not support fetch queries
            fetchCount = -1
        }

        if err == nil && result.GetBuf() == nil {
            panic(fmt.Errorf("result.Buffer is nil"))
        }*/
        return binding.selectQuery(q.ser.bytes(), asJson, q.ptVersions, q.fetchCount)
    }

    companion object {

/*
        val ANY = 0
        val EQ = 1
        val LT = 2
        val LE = 3
        val GT = 4
        val GE = 5
        val RANGE = 6
        val SET = 7
        val ALLSET = 8
        val EMPTY = 9
*/

        @JvmStatic
        fun newReindexer(url: String): Reindexer {
            val protocol = url.substring(0, url.indexOf(":"))
            when (protocol) {
                "cproto" -> return Reindexer(url, Cproto())
                "http" ->
                    //return new Reindex(url, new RestApiBinding());
                    throw UnimplementedException()
                "builtin" ->
                    //return new Reindex(url, new BuiltinBinding());
                    throw UnimplementedException()
                "builtinserver" -> throw UnimplementedException()
                else -> throw IllegalArgumentException()
            }
        }

    }
}

