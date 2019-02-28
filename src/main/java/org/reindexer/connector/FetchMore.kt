package org.reindexer.connector

import org.reindexer.connector.Err

interface FetchMore {

    fun fetch(offset: Int, limit: Int, withItems: Boolean): Err

}
