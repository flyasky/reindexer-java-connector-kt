package org.reindexer.connector.exceptions

open class ReindexerException : RuntimeException {

    constructor() {}

    constructor(message: String) : super(message) {}


    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(cause: Throwable) : super(cause) {}

}
