package org.reindexer.connector.query

import com.sun.xml.internal.fastinfoset.util.StringArray
import org.reindexer.connector.Reindexer
import org.reindexer.connector.bindings.Consts
import org.reindexer.connector.cjson.ByteArraySerializer
import org.reindexer.connector.cjson.Serializer
import org.reindexer.connector.exceptions.ReindexerException
import org.reindexer.connector.exceptions.UnimplementedException
import org.reindexer.connector.query.iterator.Iterator

class Query {

    // Constants for query serialization
    val queryCondition      = Consts.QueryCondition
    val queryDistinct       = Consts.QueryDistinct
    val querySortIndex      = Consts.QuerySortIndex
    val queryJoinOn         = Consts.QueryJoinOn
    val queryLimit          = Consts.QueryLimit
    val queryOffset         = Consts.QueryOffset
    val queryReqTotal       = Consts.QueryReqTotal
    val queryDebugLevel     = Consts.QueryDebugLevel
    val queryAggregation    = Consts.QueryAggregation
    val querySelectFilter   = Consts.QuerySelectFilter
    val queryExplain        = Consts.QueryExplain
    val querySelectFunction = Consts.QuerySelectFunction
    val queryEqualPosition  = Consts.QueryEqualPosition
    val queryUpdateField    = Consts.QueryUpdateField
    val queryEnd            = Consts.QueryEnd

    // Constants for calc total
    val modeNoCalc        = Consts.ModeNoCalc
    val modeCachedTotal   = Consts.ModeCachedTotal
    val modeAccurateTotal = Consts.ModeAccurateTotal

    // Operator
    val opAND = Consts.OpAnd
    val opOR  = Consts.OpOr
    val opNOT = Consts.OpNot


    // Join type
    val innerJoin   = Consts.InnerJoin
    val orInnerJoin = Consts.OrInnerJoin
    val leftJoin    = Consts.LeftJoin
    val merge       = Consts.Merge

    //val cInt32Max      = Consts.CInt32Max
    val valueInt       = Consts.ValueInt
    val valueBool      = Consts.ValueBool
    val valueInt64     = Consts.ValueInt64
    val valueDouble    = Consts.ValueDouble
    val valueString    = Consts.ValueString
    val valueComposite = Consts.ValueComposite
    val valueTuple     = Consts.ValueTuple

    val defaultFetchCount = 100


    // fields

    lateinit var namespace: String
    lateinit var db: Reindexer
    var nextOp: Int = opAND
    lateinit var ser: Serializer
    lateinit var root: Query
    lateinit var joinQueries: Array<Query>
    lateinit var mergedQueries: Array<Query>
    lateinit var joinToFields: StringArray
    //lateinit var joinHandlers:  []JoinHandler
    lateinit var context: Any
    var joinType: Int = innerJoin
    var closed: Boolean = true
    lateinit var initBuf: ByteArray //[256]byte
    //lateinit var nsArray:       []nsArrayEntry
    lateinit var ptVersions: IntArray
    //lateinit var iterator:      Iterator
    //lateinit var jsonIterator:  JSONIterator
    lateinit var items: List<Any>
    lateinit var json: ByteArray
    lateinit var jsonOffsets: IntArray
    lateinit var totalName: String
    var executed: Boolean = false
    var fetchCount: Int = defaultFetchCount

    public fun where(index: String, condition: Int, vararg keys: Any): Query {

        //    t := reflect.TypeOf(keys)
        //    v := reflect.ValueOf(keys)

        ser.putVarCUInt(queryCondition)
        ser.putVString(index)
        ser.putVarCUInt(nextOp)
        ser.putVarCUInt(condition)
        nextOp = opAND

        if (keys == null) {
            ser.putVarUInt(0)
            /*} else if t.Kind() == reflect.Slice || t.Kind() == reflect.Array {
            q.ser.PutVarCUInt(v.Len())
            for i := 0; i < v.Len(); i++ {
                q.putValue(v.Index(i))
            }
         */
        } else {
            ser.putVarCUInt(1)
            putValue(keys[0]) // TODO array
        }
        return this
    }

    private fun putValue(v: Any) {
        /*k := v.Kind()
        if k == reflect.Ptr || k == reflect.Interface {
            v = v.Elem()
            k = v.Kind()
        }*/

        if (v is Boolean) {
            ser.putVarCUInt(valueBool)
            if (v) {
                ser.putVarUInt(1)
            } else {
                ser.putVarUInt(0)
            }
        } else if (v is UInt) {
        /*    if unsafe.Sizeof(int(0)) == unsafe.Sizeof(int64(0)) {
                q.ser.PutVarCUInt(valueInt64)
            } else {
                q.ser.PutVarCUInt(valueInt)
            }
            q.ser.PutVarInt(int64(v.Uint()))
            */
        } else if (v is Integer || v is Long) {
//            if unsafe.Sizeof(int(0)) == unsafe.Sizeof(int64(0)) {
            ser.putVarCUInt(valueInt64)
            /*} else {
                ser.PutVarCUInt(valueInt)
            }*/
            ser.putVarInt(v as Long)/*
        } else if (v is Int16, reflect.Int32, reflect.Int8) {
            q.ser.PutVarCUInt(valueInt)
            q.ser.PutVarInt(v.Int())
        }
            case reflect.Uint8, reflect.Uint16, reflect.Uint32:
            q.ser.PutVarCUInt(valueInt)
            q.ser.PutVarInt(int64(v.Uint()))
            case reflect.Int64:
            q.ser.PutVarCUInt(valueInt64)
            q.ser.PutVarInt(v.Int())
            case reflect.Uint64:
            q.ser.PutVarCUInt(valueInt64)
            q.ser.PutVarInt(int64(v.Uint()))
            case reflect.String:
            q.ser.PutVarCUInt(valueString)
            q.ser.PutVString(v.String())
            case reflect.Float32, reflect.Float64:
            q.ser.PutVarCUInt(valueDouble)
            q.ser.PutDouble(v.Float())
            case reflect.Slice, reflect.Array:
            q.ser.PutVarCUInt(valueTuple)
            q.ser.PutVarCUInt(v.Len())
            for i := 0; i < v.Len(); i++ {
            q.putValue(v.Index(i))*/
        } else {
            throw ReindexerException("rq: Invalid type")
        }
    }

    // Exec will execute query, and return slice of items
    fun exec(): Iterator? {
        /*if q.root != nil {
            q = q.root
        }
        if q.closed {
            panic(errors.New("Exec call on already closed query. You shoud create new Query"))
        }
        if q.executed {
            panic(errors.New("Exec call on already executed query. You shoud create new Query"))
        }
        */

        executed = true
        return db.execQuery(this)
    }


    // MustExec will execute query, and return iterator, panic on error
    fun mustExec(): Iterator {
        try {
            return exec()!! //FIXME
        } catch (e: Exception) {
            throw ReindexerException(e)
        }
    }

    /**
     * // Get will execute query, and return 1 st item, panic on error

     */
    fun <T> get(): T {
        /*iter := q.Limit(1).MustExec()
        defer iter.Close()
        if iter.Next() {
            return iter.Object(), true
        }
        return nil, false*/
        throw UnimplementedException()
    }

    companion object {
        fun newQuery(db: Reindexer, namespace: String): Query {
            val q = Query()

            // TODO query pool
            /*obj := queryPool.Get()
            if obj != nil {
                q = obj.(*Query)
            }*/
            /*if q == nil {*/
                q.ser = ByteArraySerializer.newSerializer()
            /*} else {
                q.nextOp = 0
                q.root = nil
                q.joinType = 0
                q.context = nil
                q.joinToFields = q.joinToFields[:0]
                q.joinQueries = q.joinQueries[:0]
                q.joinHandlers = q.joinHandlers[:0]
                q.mergedQueries = q.mergedQueries[:0]
                q.ptVersions = q.ptVersions[:0]
                q.ser = cjson.NewSerializer(q.ser.Bytes()[:0])
                q.closed = false
                q.totalName = ""
                q.executed = false
                q.nsArray = q.nsArray[:0]
            }*/
            q.namespace = namespace
            q.db = db
            q.ser.putVString(namespace)
            return q
        }
    }
}
