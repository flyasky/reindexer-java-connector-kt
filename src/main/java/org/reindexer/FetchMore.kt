package org.reindexer

interface FetchMore {

    fun fetch(offset: Int, limit: Int, withItems: Boolean): Err

}
