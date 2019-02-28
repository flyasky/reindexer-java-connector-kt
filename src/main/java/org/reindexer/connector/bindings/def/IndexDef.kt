package org.reindexer.connector.bindings.def

import org.reindexer.connector.bindings.Consts
import org.reindexer.utils.IndexOptions

data class IndexDef(var name: String = "id") {     //`json:"name"`
    lateinit var JSONPaths: List<String>   //`json:"json_paths"`
    var indexType: String = "pk"      //`json:"index_type"`
    lateinit var fieldType: String      //`json:"field_type"`
    var isPK: Boolean = true           //`json:"is_pk"`
    var isArray: Boolean = false        //`json:"is_array"`
    var isDense: Boolean = false        //`json:"is_dense"`
    var isSparse: Boolean = false       //`json:"is_sparse"`
    lateinit var collateMode: String    //`json:"collate_mode"`
    lateinit var sortOrder: String      //`json:"sort_order_letters"`
    //lateinit var config: Any            //`json:"config"` //TODO

    private constructor(name: String, jsonPath: List<String>, idxType: String,
                fieldType: String, isPk: Boolean, isArray: Boolean, isDense: Boolean,
                isSparse: Boolean, collateMode: String, sortOrder: String) : this() {
        this.JSONPaths = jsonPath
        this.name = name
        this.fieldType = fieldType
        this.indexType = idxType
        this.isPK = isPk
        this.isArray = isArray
        this.isDense = isDense
        this.isSparse = isSparse
        this.collateMode = collateMode
        this.sortOrder = sortOrder
    }

    companion object {
        fun makeIndexDef(index: String, jsonPaths: List<String>, indexType: String,
                         fieldType: String, opts: IndexOptions,
                         collateMode: Int, sortOrder: String) : IndexDef {
            var cm = ""
            when (collateMode) {
                Consts.CollateASCII -> cm = "ascii"
                Consts.CollateUTF8 -> cm = "utf8"
                Consts.CollateNumeric -> cm = "numeric"
                Consts.CollateCustom -> cm = "custom"
            }
            return IndexDef(
                index,
                jsonPaths,
                indexType,
                fieldType,
                opts.isArray,
                opts.isPk,
                opts.isDense,
                opts.isSparse,
                cm,
                sortOrder
            )
        }
    }
}