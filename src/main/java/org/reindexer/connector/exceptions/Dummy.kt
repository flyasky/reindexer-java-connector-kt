package org.reindexer.connector.exceptions

open class Dummy : ReindexerException {

    constructor()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)

}
