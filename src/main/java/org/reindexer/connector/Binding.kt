package org.reindexer.connector

import org.reindexer.connector.def.IndexDef

/**
 * Raw binding to reindexer
 */
interface Binding {

    /**
     *
     *
     * Init(u *url.URL, options ...interface{}) error
     *
     * @param url
     * @param options
     */
    fun init(url: String, vararg options: Any): Err

    fun openNamespace(namespace: String, enableStorage: Boolean, dropOnFileFormatError: Boolean): Err
    fun closeNamespace(namespace: String): Err
    fun dropNamespace(namespace: String): Err
    fun enableStorage(namespace: String): Err

    fun addIndex(namespace: String, indexDef: IndexDef): Err
    fun updateIndex(namespace: String, indexDef: IndexDef): Err
    fun dropIndex(namespace: String, index: String): Err

    /**
     *
     *
     * ModifyItem(nsHash int, namespace string, format int, data []byte, mode int, percepts []string, stateToken int) (RawBuffer, error)
     *
     * @param nsHash
     * @param namespace
     * @param format
     * @param data
     * @param mode
     * @param percepts
     * @param stateToken
     * @return
     */
    fun modifyItem(nsHash: Int, namespace: String, format: Int, data: ByteArray, mode: Int, percepts: Array<String>,
                   stateToken: Int): Res

    /*
        Clone() RawBinding

        BeginTx(namespace string) (TxCtx, error)
        CommitTx(*TxCtx) (RawBuffer, error)
        RollbackTx(*TxCtx) error
        ModifyItemTx(txCtx *TxCtx, format int, data []byte, mode int, percepts []string, stateToken int) error

        PutMeta(namespace, key, data string) error
        GetMeta(namespace, key string) (RawBuffer, error)
        Select(query string, withItems bool, ptVersions []int32, fetchCount int) (RawBuffer, error)
        SelectQuery(rawQuery []byte, withItems bool, ptVersions []int32, fetchCount int) (RawBuffer, error)
        DeleteQuery(nsHash int, rawQuery []byte) (RawBuffer, error)
        UpdateQuery(nsHash int, rawQuery []byte) (RawBuffer, error)
        Commit(namespace string) error
        EnableLogger(logger Logger)
        DisableLogger()
        Ping() error
        Finalize() error
        Status() Status*/

}
