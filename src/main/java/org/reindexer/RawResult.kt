package org.reindexer

import org.reindexer.connector.RawBuffer

/**
 * Created by Anton Burkun on 27.02.19.
 */
data class RawResult(val error: Int, val result: RawBuffer?)