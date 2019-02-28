package org.reindexer.connector.bindings

import org.reindexer.connector.bindings.Err

interface FetchMore {

    fun fetch(offset: Int, limit: Int, withItems: Boolean): Err

}
