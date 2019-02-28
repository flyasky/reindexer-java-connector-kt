package org.reindexer.utils;

import org.reindexer.connector.bindings.def.IndexDef
import java.nio.ByteBuffer

/**
 *
 */
object Reflect {

    fun parseIndex(namespace: String, st: Class<*>, joined: Map<String, IntArray>): List<IndexDef> {

        val res = ArrayList<IndexDef>()
        parse(res, st, false, "", "", joined)
        return res
    }

    fun parse(indexDefs: List<IndexDef>, st: Class<*>, subArray: Boolean, aReindexBasePath: String, aJsonBasePath: String,
              joined: Map<String, IntArray>) {

        var jsonBasePath = aJsonBasePath
        if (jsonBasePath.length != 0 && !jsonBasePath.endsWith(".")) {
            jsonBasePath += "."
        }

        var reindexBasePath = aReindexBasePath
        if (reindexBasePath.length != 0 && !reindexBasePath.endsWith(".")) {
            reindexBasePath += "."
        }

        for (i in 0..st.fields.size - 1) {

        }


        /*

            if st.Kind() == reflect.Ptr {
                st = st.Elem()
            }

            for i := 0; i < st.NumField(); i++ {

                t := st.Field(i).Type
                if t.Kind() == reflect.Ptr {
                    t = t.Elem()
                }
                // Get and parse tags
                tagsSlice := strings.SplitN(st.Field(i).Tag.Get("reindex"), ",", 3)
                jsonPath := strings.Split(st.Field(i).Tag.Get("json"), ",")[0]

                if len(jsonPath) == 0 && !st.Field(i).Anonymous {
                    jsonPath = st.Field(i).Name
                }
                jsonPath = jsonBasePath + jsonPath

                idxName, idxType, idxOpts := tagsSlice[0], "", ""
                if idxName == "-" {
                    continue
                }

                if len(tagsSlice) > 1 {
                    idxType = tagsSlice[1]
                }
                if len(tagsSlice) > 2 {
                    idxOpts = tagsSlice[2]
                }

                reindexPath := reindexBasePath + idxName

                idxSettings := splitOptions(idxOpts)

                opts := parseOpts(&idxSettings)
                if t.Kind() == reflect.Slice || t.Kind() == reflect.Array || subArray {
                    opts.isArray = true
                }

                if opts.isPk && strings.TrimSpace(idxName) == "" {
                    return fmt.Errorf("No index name is specified for primary key in field %s", st.Field(i).Name)
                }

                if parseByKeyWord(&idxSettings, "composite") {
                    if t.Kind() != reflect.Struct || t.NumField() != 0 {
                        return fmt.Errorf("'composite' tag allowed only on empty on structs: Invalid tags %v on field %s", tagsSlice, st.Field(i).Name)
                    }

                    indexDef := makeIndexDef(parseCompositeName(reindexPath), parseCompositeJsonPaths(reindexPath), idxType, "composite", opts, CollateNone, "")
                    if err := indexDefAppend(indexDefs, indexDef, opts.isAppenable); err != nil {
                        return err
                    }
                } else if t.Kind() == reflect.Struct {
                    if err := parse(indexDefs, t, subArray, reindexPath, jsonPath, joined); err != nil {
                        return err
                    }
                } else if (t.Kind() == reflect.Slice || t.Kind() == reflect.Array) &&
                    (t.Elem().Kind() == reflect.Struct || (t.Elem().Kind() == reflect.Ptr && t.Elem().Elem().Kind() == reflect.Struct)) {
                    // Check if field nested slice of struct
                    if parseByKeyWord(&idxSettings, "joined") && len(idxName) > 0 {
                        (*joined)[tagsSlice[0]] = st.Field(i).Index
                    } else if err := parse(indexDefs, t.Elem(), true, reindexPath, jsonPath, joined); err != nil {
                        return err
                    }
                } else if len(idxName) > 0 {
                    collateMode, sortOrderLetters := parseCollate(&idxSettings)

                    if fieldType, err := getFieldType(t); err != nil {
                        return err
                    } else {
                        indexDef := makeIndexDef(reindexPath, []string{jsonPath}, idxType, fieldType, opts, collateMode, sortOrderLetters)
                        if err := indexDefAppend(indexDefs, indexDef, opts.isAppenable); err != nil {
                            return err
                        }
                    }
                }
                if len(idxSettings) > 0 {
                    return fmt.Errorf("Unknown index settings are found: %v", idxSettings)
                }

            }

            return nil
        }

         */
    }

    fun splitOptions(str: String): List<String> {
        val words = ArrayList<String>()
        var word: ByteBuffer = ByteBuffer.allocate(10)
        val strLen = str.length
        var i = 0
        while (i < strLen) {
            if (str[i] == '\\' && i < strLen-1 && str[i+1] == ',') {
                word.put(str[i+1].toByte())
                i++
                continue
            }

            if (str[i] == ',') {
                words.add(word.toString())
                word.clear()
                continue
            }

            word.put(str[i].toByte())

            if (i == strLen-1) {
                words.add(word.toString())
                word.clear()
                continue
            }
            i++
        }
        return words
    }

    fun parseOpts(idxSettingsBuf: List<String>): IndexOptions {
        val newIdxSettingsBuf = ArrayList<String>()

        val opts = IndexOptions()

        for (idxSetting in idxSettingsBuf) {
            when (idxSetting) {
                "pk" -> opts.isPk = true
                "dense" -> opts.isDense = true
                "sparse" -> opts.isSparse = true
                "appendable" -> opts.isAppenable = true
                else -> newIdxSettingsBuf.add(idxSetting)
            }
        }
        //idxSettingsBuf = newIdxSettingsBuf    //???
        return opts
    }


}
