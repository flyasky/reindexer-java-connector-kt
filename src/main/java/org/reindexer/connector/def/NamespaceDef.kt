package org.reindexer.connector.def

import org.reindexer.connector.options.StorageOpts

/*
type NamespaceDef struct {
	Namespace   string      `json:"name"`
	StorageOpts StorageOpts `json:"storage"`
}
 */
class NamespaceDef
//"indexes":[]

(val storage: StorageOpts, val name: String)
