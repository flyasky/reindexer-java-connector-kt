package org.reindexer.connector.bindings.def

data class IndexDef(val name: String) {     //`json:"name"`
    lateinit var JSONPaths: Array<String>   //`json:"json_paths"`
    lateinit var indexType: String      //`json:"index_type"`
    lateinit var fieldType: String      //`json:"field_type"`
    var isPK: Boolean = false           //`json:"is_pk"`
    var isArray: Boolean = false        //`json:"is_array"`
    var isDense: Boolean = false        //`json:"is_dense"`
    var isSparse: Boolean = false       //`json:"is_sparse"`
    lateinit var collateMode: String    //`json:"collate_mode"`
    lateinit var sortOrder: String      //`json:"sort_order_letters"`
    lateinit var config: Any            //`json:"config"`
}