package org.reindexer.utils;

import org.reindexer.annotations.Reindex
import org.reindexer.connector.bindings.Consts
import org.reindexer.connector.bindings.def.IndexDef
import org.reindexer.connector.exceptions.ReindexerException
import java.lang.reflect.Type
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

    fun parse(indexDefs: ArrayList<IndexDef>, st: Class<*>, subArray: Boolean, aReindexBasePath: String, aJsonBasePath: String,
              joined: Map<String, IntArray>) {

        var jsonBasePath = aJsonBasePath
        if (jsonBasePath.length != 0 && !jsonBasePath.endsWith(".")) {
            jsonBasePath += "."
        }

        var reindexBasePath = aReindexBasePath
        if (reindexBasePath.length != 0 && !reindexBasePath.endsWith(".")) {
            reindexBasePath += "."
        }

        for (i in 0..st.declaredFields.size - 1) {
            val tagsSlice = st.declaredFields[i].getAnnotation(Reindex::class.java).value.split(",", limit=3)
            val idxName = tagsSlice[0]
            val idxType = ""
            val idxOpts = ""
            if (idxName == "-") {
                continue
            }
            var reindexPath = reindexBasePath + idxName
            var idxSettings = splitOptions(idxOpts)
            var (opts, idxSettingsNew) = parseOpts(idxSettings)
            idxSettings = idxSettingsNew

            if (opts.isPk && idxName.trim() == "") {
                throw ReindexerException("No index name is specified for primary key in field $st.fields[i].name")
            }

            if (1 == 1) {

            } else if (idxName.length > 0) {
                //collateMode, sortOrderLetters := parseCollate(&idxSettings)
                /*if fieldType, err := getFieldType(t); err != nil {
                return err*/
            } else {
                val fieldType = getFieldType(st)

                var indexDef = IndexDef.makeIndexDef(reindexPath, emptyList(), idxType, fieldType, opts,
                        Consts.CollateNone, "")
                indexDefAppend(indexDefs, indexDef, opts.isAppenable)
            }
            if (idxSettings.size > 0) {
                throw ReindexerException("Unknown index settings are found: $idxSettings")
            }
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

    private fun indexDefAppend(indexDefs: ArrayList<IndexDef>, indexDef: IndexDef, appenable: Boolean) {
        indexDefs.add(indexDef)
        /*lang:go
            name := indexDef.Name

            var foundIndexPos int
            var foundIndexDef bindings.IndexDef

            indexDefExists := false
            for pos, indexDef := range *indexDefs {
                if (*indexDefs)[pos].Name == name {
                    indexDefExists = true
                    foundIndexDef = indexDef
                    foundIndexPos = pos

                    break
                }
            }

            if !indexDefExists {
                *indexDefs = append(*indexDefs, indexDef)

                return nil
            }

            if indexDef.IndexType != foundIndexDef.IndexType {
                return fmt.Errorf("Index %s has another type: %+v", name, indexDef)
            }

            if len(indexDef.JSONPaths) > 0 && indexDef.IndexType != "composite" {
                jsonPaths := foundIndexDef.JSONPaths
                isPresented := false
                for _, jsonPath := range jsonPaths {
                    if jsonPath == indexDef.JSONPaths[0] {
                        isPresented = true
                        break
                    }
                }

                if !isPresented {
                    if !isAppendable {
                        return fmt.Errorf("Index %s is not appendable", name)
                    }

                    foundIndexDef.JSONPaths = append(foundIndexDef.JSONPaths, indexDef.JSONPaths[0])
                }

                foundIndexDef.IsArray = true
                (*indexDefs)[foundIndexPos] = foundIndexDef
            }
            return nil

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

    fun parseOpts(idxSettingsBuf: List<String>): Pair<IndexOptions, List<String>> {
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
        return Pair(opts, newIdxSettingsBuf)
    }

    fun getFieldType(t: Type): String {
        when (t.typeName) {
            "Boolean" -> return "bool"
            "Short", "Integer", "UInt16", "UInt32" -> return "int"
            /*case reflect.Int, reflect.Uint:
            if unsafe.Sizeof(int(0)) == unsafe.Sizeof(int64(0)) {
                return "int64", nil
            } else {
                return "int", nil
            }*/
            "int", "Int" -> return "int"
            "Int64", "Uint64" -> return "int64"
            "String" -> return "string"
            "Double", "Float" -> return "double"
            /*case reflect.Struct:
            return "composite", nil
            case reflect.Array, reflect.Slice, reflect.Ptr:
            return getFieldType(t.Elem())*/
        }
        throw ReindexerException("Invalid reflection")
    }


}
