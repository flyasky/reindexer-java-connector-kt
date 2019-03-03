package org.reindexer.connector.bindings.def

import org.reindexer.connector.bindings.Consts
import org.reindexer.utils.IndexOptions

data class IndexDef(var name: String = "id") {
    lateinit var jsonPaths: List<String>
    var indexType: String = "pk"
    lateinit var fieldType: String
    var isPk: Boolean = true
    var isArray: Boolean = false
    var isDense: Boolean = false
    var isSparse: Boolean = false
    lateinit var collateMode: String
    lateinit var sortOrder: String
    //lateinit var config: Any //`json:"config"` //TODO

    private constructor(name: String, jsonPath: List<String>, idxType: String,
                        fieldType: String, isPk: Boolean, isArray: Boolean, isDense: Boolean,
                        isSparse: Boolean, collateMode: String, sortOrder: String) : this() {
        this.jsonPaths = jsonPath
        this.name = name
        this.fieldType = fieldType
        this.indexType = idxType
        this.isPk = isPk
        this.isArray = isArray
        this.isDense = isDense
        this.isSparse = isSparse
        this.collateMode = collateMode
        this.sortOrder = sortOrder
    }

    companion object {
        fun makeIndexDef(index: String, jsonPaths: List<String>, indexType: String,
                         fieldType: String, opts: IndexOptions,
                         collateMode: Int, sortOrder: String): IndexDef {
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
                    opts.isPk,
                    opts.isArray,
                    opts.isDense,
                    opts.isSparse,
                    cm,
                    sortOrder
            )
        }
    }
}