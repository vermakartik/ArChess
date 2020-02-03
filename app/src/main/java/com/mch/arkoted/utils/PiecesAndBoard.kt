package com.mch.arkoted.utils

import android.app.Activity
import android.widget.Toast
import com.google.ar.sceneform.rendering.ModelRenderable
import com.mch.arkoted.R

object PiecesAndBoard {

    var chessMap = mutableMapOf<Int, ModelRenderable>()

    val PAWN_BLACK = 0x10
    val KNIGHT_BLACK = 0x11
    val KING_BLACK = 0x12
    val QUEEN_BLACK = 0x13
    val BISHOP_BLACK = 0x14
    val ROOK_BLACK = 0x15

    val PAWN_WHITE= 0x20
    val KNIGHT_WHITE= 0x21
    val KING_WHITE= 0x22
    val QUEEN_WHITE= 0x23
    val BISHOP_WHITE= 0x24
    val ROOK_WHITE= 0x25

    val BOARD = 0x30
    val COVER = 0x40

    val piecesList = mutableMapOf(

        PAWN_BLACK to R.raw.pawn_black,
        KING_BLACK to R.raw.king_black,
        QUEEN_BLACK to R.raw.queen_black,
        KNIGHT_BLACK to R.raw.knight_black,
        ROOK_BLACK to R.raw.rook_black,
        BISHOP_BLACK to R.raw.bishop_black,

        PAWN_WHITE to R.raw.pawn_white,
        KING_WHITE to R.raw.king_white,
        QUEEN_WHITE to R.raw.queen_white,
        KNIGHT_WHITE to R.raw.knight_white,
        ROOK_WHITE to R.raw.rook_white,
        BISHOP_WHITE to R.raw.bishop_white
    )

    val BOARD_MODEL_CONST = R.raw.chess_board
    val COVER_MODEL_CONST = R.raw.cover

    fun checkIfLoadedAll(activity: Activity){

//        1 is added for board count
        if(chessMap.size == 1 + piecesList.size) {
            Toast.makeText(activity.applicationContext, "Loaded All Pieces", Toast.LENGTH_LONG).show()
        }
    }

    fun loadAllPieces(assetLoader: AssetLoader, activity: Activity) {
        assetLoader.setLoaderFunc {i, modelRenderable ->
            chessMap[i] = modelRenderable
            checkIfLoadedAll(activity)
        }
        piecesList.all {
            assetLoader.loadAsset(it.key, it.value)
        }
        assetLoader.loadAsset(
            BOARD,
            BOARD_MODEL_CONST
        )
        assetLoader.loadAsset(
            COVER,
            COVER_MODEL_CONST
        )
    }

}