package org.reindexer.connector.options

/*
type StorageOpts struct {
        EnableStorage     bool `json:"enabled"`
        DropOnFormatError bool `json:"drop_on_file_format_error"`
        CreateIfMissing   bool `json:"create_if_missing"`
}
*/
class StorageOpts(val isEnabled: Boolean, val isDropOnFileFormatError: Boolean, val isCreateIfMissing: Boolean)/*

    type StorageOptions uint16

    func (so *StorageOptions) Enabled(value bool) *StorageOptions {
        if value {
            *so |= StorageOptions(StorageOptEnabled | StorageOptCreateIfMissing)
        } else {
            *so &= ^StorageOptions(StorageOptEnabled)
        }
        return so
    }

    func (so *StorageOptions) DropOnFileFormatError(value bool) *StorageOptions {
        if value {
            *so |= StorageOptions(StorageOptDropOnFileFormatError)
        } else {
            *so &= ^StorageOptions(StorageOptDropOnFileFormatError)
        }
        return so
    }
    */
