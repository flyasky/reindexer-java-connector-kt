package org.reindexer.connector.bindings

import org.reindexer.connector.bindings.Err
import org.reindexer.connector.bindings.RawBuffer

data class Res(val error: Err, val rawBuffer: RawBuffer)