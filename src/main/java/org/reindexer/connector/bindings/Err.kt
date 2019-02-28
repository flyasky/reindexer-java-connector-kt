package org.reindexer.connector.bindings

// struct Error from interface.go
data class Err(val message: String, val code: Int) {

    fun isOk(): Boolean {
        return code == 0
    }

    companion object {
        fun newError(s: String, code: Int): Err {
            return Err(s, code)
        }

        fun newOk(): Err {
            return Err("OK", 0)
        }
    }
}