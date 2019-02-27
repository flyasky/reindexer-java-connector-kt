package org.reindexer.connector

import org.reindexer.cproto.CprotoBinding
import org.reindexer.exceptions.UnimplementedException

class ReindexerFactoryImpl : ReindexerFactory {

    override fun newReindexer(url: String): Reindexer {
        val protocol = url.substring(0, url.indexOf(":"))
        when (protocol) {
            "cproto" -> return Reindexer(url, CprotoBinding())
            "http" ->
                //return new Reindexer(url, new RestApiBinding());
                throw UnimplementedException()
            "builtin" ->
                //return new Reindexer(url, new BuiltinBinding());
                throw UnimplementedException()
            "builtinserver" -> throw UnimplementedException()
            else -> throw IllegalArgumentException()
        }
    }
}
