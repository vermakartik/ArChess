package com.mch.arkoted.utils

import com.mch.arkoted.dtypes.Point
import com.mch.arkoted.dtypes.Square

class BoardConfigurator {

    var boardConfiguration = MutableList(ChessRenderer.BOARD_COL_COUNT * ChessRenderer.BOARD_COL_COUNT / 2) { FLUSH_BYTE}

    var whitePlayerState = PlayerState(PieceColor.WHITE)
    var blackPlayerState = PlayerState(PieceColor.BLACK)

    var positionMap = hashMapOf<Byte, MutableSet<Int>>()

    fun initBoard(){

        for(i in 0..boardConfiguration.size-1) {
            boardConfiguration[i] = FLUSH_BYTE
        }
        positionMap.clear()
        positionMap[makePiece(PieceColor.BLACK, PieceType.PAWN)] = mutableSetOf()
        positionMap[makePiece(PieceColor.WHITE, PieceType.PAWN)] = mutableSetOf()
        positionMap[makePiece(PieceColor.BLACK, PieceType.KING)] =  hashSetOf(ChessRenderer.locationHash(Point( 4f, 0f)))
        positionMap[makePiece(PieceColor.BLACK, PieceType.QUEEN)] =  hashSetOf(ChessRenderer.locationHash(Point( 3f, 0f)))
        positionMap[makePiece(PieceColor.BLACK, PieceType.BISHOP)] =  hashSetOf(ChessRenderer.locationHash(Point( 2f, 0f)), ChessRenderer.locationHash(Point(5f, 0f)))
        positionMap[makePiece(PieceColor.BLACK, PieceType.KNIGHT)] =  hashSetOf(ChessRenderer.locationHash(Point( 1f, 0f)), ChessRenderer.locationHash(Point(6f, 0f)))
        positionMap[makePiece(PieceColor.BLACK, PieceType.ROOK)] =  hashSetOf(ChessRenderer.locationHash(Point( 0f, 0f)), ChessRenderer.locationHash(Point(7f, 0f)))

        positionMap[makePiece(PieceColor.WHITE, PieceType.KING)] =  hashSetOf(ChessRenderer.locationHash(Point(4f, 7f)))
        positionMap[makePiece(PieceColor.WHITE, PieceType.QUEEN)] =  hashSetOf(ChessRenderer.locationHash(Point(3f, 7f)))
        positionMap[makePiece(PieceColor.WHITE, PieceType.BISHOP)] =  hashSetOf(ChessRenderer.locationHash(Point(5f, 7f)), ChessRenderer.locationHash(Point(2f, 7f)))
        positionMap[makePiece(PieceColor.WHITE, PieceType.KNIGHT)] =  hashSetOf(ChessRenderer.locationHash(Point(1f, 7f)), ChessRenderer.locationHash(Point(6f, 7f)))
        positionMap[makePiece(PieceColor.WHITE, PieceType.ROOK)] =  hashSetOf(ChessRenderer.locationHash(Point(0f, 7f)), ChessRenderer.locationHash(Point(7f, 7f)))

        for(i in 0..ChessRenderer.BOARD_ROW_COUNT-1) {
            setByteForLocation(Point(i.toFloat(), 1f), makePiece(PieceColor.BLACK, PieceType.PAWN))
            setByteForLocation(Point(i.toFloat(), 6f), makePiece(PieceColor.WHITE, PieceType.PAWN))

            positionMap[makePiece(PieceColor.WHITE, PieceType.PAWN)]!!.add(ChessRenderer.locationHash(Point(i.toFloat(), 6f)))
            positionMap[makePiece(PieceColor.BLACK, PieceType.PAWN)]!!.add(ChessRenderer.locationHash(Point(i.toFloat(), 1f)))

        }

        setByteForLocation(Point(4f, 0f), makePiece(PieceColor.BLACK, PieceType.KING))
        setByteForLocation(Point(3f, 0f), makePiece(PieceColor.BLACK, PieceType.QUEEN))
        setByteForLocation(Point(2f, 0f), makePiece(PieceColor.BLACK, PieceType.BISHOP))
        setByteForLocation(Point(1f, 0f), makePiece(PieceColor.BLACK, PieceType.KNIGHT))
        setByteForLocation(Point(0f, 0f), makePiece(PieceColor.BLACK, PieceType.ROOK))
        setByteForLocation(Point(5f, 0f), makePiece(PieceColor.BLACK, PieceType.BISHOP))
        setByteForLocation(Point(6f, 0f), makePiece(PieceColor.BLACK, PieceType.KNIGHT))
        setByteForLocation(Point(7f, 0f), makePiece(PieceColor.BLACK, PieceType.ROOK))

        setByteForLocation(Point(4f, 7f), makePiece(PieceColor.WHITE, PieceType.KING))
        setByteForLocation(Point(3f, 7f), makePiece(PieceColor.WHITE, PieceType.QUEEN))
        setByteForLocation(Point(2f, 7f), makePiece(PieceColor.WHITE, PieceType.BISHOP))
        setByteForLocation(Point(1f, 7f), makePiece(PieceColor.WHITE, PieceType.KNIGHT))
        setByteForLocation(Point(0f, 7f), makePiece(PieceColor.WHITE, PieceType.ROOK))
        setByteForLocation(Point(5f, 7f), makePiece(PieceColor.WHITE, PieceType.BISHOP))
        setByteForLocation(Point(6f, 7f), makePiece(PieceColor.WHITE, PieceType.KNIGHT))
        setByteForLocation(Point(7f, 7f), makePiece(PieceColor.WHITE, PieceType.ROOK))


        whitePlayerState.initInfo()
        blackPlayerState.initInfo()

    }

    fun getShiftCount(i: Int): Int {
        return HIGHER_BYTE_SHIFT_COUNT * (1 - i)
    }

    fun getShifter(i: Int): Int{
        return (0x0F shl (HIGHER_BYTE_SHIFT_COUNT * (1 - i))) and 0xFF
    }

    fun setByteForLocation(point: Point, b: Byte){

        val prevInfo = getPointInfo(point)

        val position = ChessRenderer.locationHash(point)
        val bPos = position / 2
        val shifter = getShiftCount(position % 2)
        val nshifter = getShifter(1 - position % 2)

        val ch = (b.toInt() and 0xFF) shl shifter
        val uch = (boardConfiguration[bPos].toInt() and 0xFF) and nshifter

        boardConfiguration[bPos] = ((ch or uch) and 0xFF).toByte()

        if(!prevInfo.empty && positionMap[makePiece(prevInfo.color as PieceColor, prevInfo.type as PieceType)]!!.contains(ChessRenderer.locationHash(point))) {
            positionMap[makePiece(prevInfo.color, prevInfo.type)]!!.remove(ChessRenderer.locationHash(point))
        }

        val curInfo = getPointInfo(point)

        if(!curInfo.empty) {
            positionMap[makePiece(curInfo.color as PieceColor, curInfo.type as PieceType)]!!.add(ChessRenderer.locationHash(point))
        }
    }

    fun getByteForPoint(p: Point): Byte{
        val position = ChessRenderer.locationHash(p)
        val bPos = position / 2
        val shiftCount: Int = getShiftCount(position % 2)
        val shifter: Int = getShifter(position % 2)
        return (((boardConfiguration[bPos].toInt() and 0xFF) and shifter) shr shiftCount).toByte()
    }

    fun getPointInfo(p: Point): Square {
        val b = getByteForPoint(p)
        if(((b.toInt() and 0x0F) and 0xFF).toByte() != FLUSH_BYTE) {
            val c = getColor(b)
            val t = getPiece(b)
            return Square(p, c, t, false)
        } else {
            return Square(p, null, null, true)
        }

    }

    fun movePiece(src: Point, dest: Point){
        val b = getByteForPoint(src)

        setByteForLocation(src, 0x0)
        setByteForLocation(dest, b)
    }



    fun getPieceLocation(pieceColor: PieceColor, pieceType: PieceType): Set<Int>? {
        return positionMap[makePiece(pieceColor, pieceType)]
    }

    companion object {
        const val FLUSH_BYTE: Byte = 0x0

        const val COLOR_MASK: Byte = 0b00001000
        const val PIECE_MASK: Byte = 0b00000111
        const val HIGHER_BYTE_SHIFT_COUNT = 4

        enum class PieceColor(val value: Byte){
            BLACK(0b00001000),
            WHITE(0b00000000);

            companion object {
                fun from(b: Byte): PieceColor = values().first {it.value == b}
                fun inverse(p: PieceColor): PieceColor {
                    return when(p) {
                        BLACK -> WHITE
                        WHITE -> BLACK
                    }
                }
            }
        }

        enum class PieceType(val value: Byte) {
            PAWN(0b00000001),
            KING(0b00000010),
            QUEEN(0b00000011),
            BISHOP(0b00000100),
            KNIGHT(0b00000101),
            ROOK(0b00000110);

            companion object {
                fun from(b: Byte): PieceType = values().first { it.value == b }
            }
        }

        fun getColor(b: Byte): PieceColor{
            return PieceColor.from(((b.toInt() and COLOR_MASK.toInt()) and 0xFF).toByte())
        }

        fun getPiece(b: Byte): PieceType {
            return PieceType.from(((b.toInt() and PIECE_MASK.toInt()) and 0xFF).toByte())
        }

        fun makePiece(c: PieceColor, t: PieceType): Byte {
            return ((c.value.toInt() or t.value.toInt()) and 0x0F).toByte()
        }

    }

    enum class CastlingSide(val b: Int) {
        LONG_SIDE(-2),
        SHORT_SIDE(2);

        companion object {
            fun from(v: Int): CastlingSide { return values().first { it.b == v } }
        }
    }

}