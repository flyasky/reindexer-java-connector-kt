package org.reindexer.connector

import org.reindexer.connector.bindings.def.IndexDef
import org.reindexer.connector.options.NamespaceOptions

// cacheItems    map[int]cacheItem
// cacheLock     sync.RWMutex
// Type rtype;   // reflect.Type
// boolean deepCopyIface;
// State cjsonState; // cjson.State
// var isOpened: Boolean = false
data class Namespace(val name: String, val clazz: Class<*>) {
    val joined: Map<String, IntArray> = HashMap()
    var indexes: List<IndexDef> = ArrayList()
    var options: NamespaceOptions = NamespaceOptions.defaultOptions()
}


