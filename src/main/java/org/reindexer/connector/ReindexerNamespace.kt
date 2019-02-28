package org.reindexer.connector

import org.reindexer.connector.bindings.def.IndexDef
import org.reindexer.connector.cjson.State
import org.reindexer.connector.options.NamespaceOptions
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

data class ReindexerNamespace(val name: String, val rtype: Type) {
    val cacheItems: Map<Int, CacheItem> = HashMap(100)
    val cacheLock: AtomicBoolean = AtomicBoolean() //sync.RWMutex
    val joined: Map<String, IntArray> = HashMap()
    var indexes: List<IndexDef> = ArrayList()
    var deepCopyIface: Boolean = false
    var opts: NamespaceOptions = NamespaceOptions.defaultOptions()
    var cjsonState: State = State()
    var nsHash: Int = hashCode()
        get() = this.hashCode()
    var opened: Boolean = false
}


