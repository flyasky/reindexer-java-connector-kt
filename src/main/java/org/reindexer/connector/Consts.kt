package org.reindexer.connector

object Consts {

    //    const CInt32Max = int(^uint32(0) >> 1)

    // public go consts from type_consts.h and reindexer_ctypes.h

    var ANY = 0
    var EQ = 1
    var LT = 2
    var LE = 3
    var GT = 4
    var GE = 5
    var RANGE = 6
    var SET = 7
    var ALLSET = 8
    var EMPTY = 9

    var ERROR = 1
    var WARNING = 2
    var INFO = 3
    var TRACE = 4

    var AggSum = 0
    var AggAvg = 1
    var AggFacet = 2
    var AggMin = 3
    var AggMax = 4

    var CollateNone = 0
    var CollateASCII = 1
    var CollateUTF8 = 2
    var CollateNumeric = 3
    var CollateCustom = 4

    // private go consts from type_consts.h and reindexer_ctypes.h

    var OpOr = 1
    var OpAnd = 2
    var OpNot = 3

    val ValueInt64 = 0
    val ValueDouble = 1
    val ValueString = 2
    val ValueBool = 3
    val ValueNull = 4
    val ValueInt = 8
    val ValueUndefined = 9
    val ValueComposite = 10
    val ValueTuple = 11

    var QueryCondition = 0
    var QueryDistinct = 1
    var QuerySortIndex = 2
    var QueryJoinOn = 3
    var QueryLimit = 4
    var QueryOffset = 5
    var QueryReqTotal = 6
    var QueryDebugLevel = 7
    var QueryAggregation = 8
    var QuerySelectFilter = 9
    var QuerySelectFunction = 10
    var QueryEnd = 11
    var QueryExplain = 12
    var QueryEqualPosition = 13
    var QueryUpdateField = 14

    var LeftJoin = 0
    var InnerJoin = 1
    var OrInnerJoin = 2
    var Merge = 3

    var CacheModeOn = 0
    var CacheModeAggressive = 1
    var CacheModeOff = 2

    var FormatJson = 0
    var FormatCJson = 1

    var ModeUpdate = 0
    var ModeInsert = 1
    var ModeUpsert = 2
    var ModeDelete = 3

    var ModeNoCalc = 0
    var ModeCachedTotal = 1
    var ModeAccurateTotal = 2

    var QueryResultEnd = 0
    var QueryResultAggregation = 1
    var QueryResultExplain = 2

    var ResultsFormatMask = 0xF
    var ResultsPure = 0x0
    var ResultsPtrs = 0x1
    var ResultsCJson = 0x2
    var ResultsJson = 0x3

    var ResultsWithPayloadTypes = 0x10
    var ResultsWithItemID = 0x20
    var ResultsWithPercents = 0x40
    var ResultsWithNsID = 0x80
    var ResultsWithJoined = 0x100

    var IndexOptPK = 1 shl 7
    var IndexOptArray = 1 shl 6
    var IndexOptDense = 1 shl 5
    var IndexOptAppendable = 1 shl 4
    var IndexOptSparse = 1 shl 3

    var StorageOptEnabled = 1
    var StorageOptDropOnFileFormatError = 1 shl 1
    var StorageOptCreateIfMissing = 1 shl 2

    var ErrOK = 0
    var ErrParseSQL = 1
    var ErrQueryExec = 2
    var ErrParams = 3
    var ErrLogic = 4
    var ErrParseJson = 5
    var ErrParseDSL = 6
    var ErrConflict = 7
    var ErrParseBin = 8
    var ErrForbidden = 9
    var ErrWasRelock = 10
    var ErrNotValid = 11
    var ErrNetwork = 12
    var ErrNotFound = 13
    var ErrStateInvalidated = 14

}
