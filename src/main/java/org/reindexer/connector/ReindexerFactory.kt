package org.reindexer.connector

interface ReindexerFactory {

    fun newReindexer(url: String): Reindexer

}
