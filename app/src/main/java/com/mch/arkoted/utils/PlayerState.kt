package com.mch.arkoted.utils

import com.mch.arkoted.dtypes.Point

class PlayerState(val pieceColor: BoardConfigurator.Companion.PieceColor) {

    var isCastlingPossible = true
    var isPawnPassantEnabled = false
    var enPassantPieceInfo: Point? = null
    var isKingInCheck: Boolean = false

    fun initInfo() {
        isCastlingPossible = true
        isPawnPassantEnabled = false
        enPassantPieceInfo = null
        isKingInCheck = false
    }
}