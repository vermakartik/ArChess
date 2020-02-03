package com.mch.arkoted.utils

import com.mch.arkoted.dtypes.Point
import kotlin.math.abs

//import kotlin.experimental.and
//import kotlin.experimental.or

object PieceConfiguration {

    enum class MoveDirections(val value: Byte){

        LEFT_STRAIGHT(0b00000001),
        RIGHT_STRAIGHT(0b00000010),
        TOP_STRAIGHT(0b00000100),
        BOTTOM_STRAIGHT(0b00001000),

        LEFT_TOP_DIAGONAL(0b00000001 or 0b00000100),
        LEFT_BOTTOM_DIAGONAL(0b00000001 or 0b00001000),
        RIGHT_TOP_DIAGONAL(0b00000010 or 0b00000100),
        RIGHT_BOTTOM_DIAGONAL(0b00000010 or 0b00001000);

        companion object {
            fun from(b: Byte): MoveDirections {
                return values().first {it.value == b}
            }
        }
    }

    val ALL_STRAIGHT = hashSetOf(
        MoveDirections.TOP_STRAIGHT,
        MoveDirections.BOTTOM_STRAIGHT,
        MoveDirections.RIGHT_STRAIGHT,
        MoveDirections.LEFT_STRAIGHT
    )

    val ALL_DIAGONALS = hashSetOf(
        MoveDirections.LEFT_TOP_DIAGONAL,
        MoveDirections.RIGHT_TOP_DIAGONAL,
        MoveDirections.LEFT_BOTTOM_DIAGONAL,
        MoveDirections.RIGHT_BOTTOM_DIAGONAL
    )

    fun generatePossiblePositions(position: Int, type: BoardConfigurator.Companion.PieceColor, pieceType: BoardConfigurator.Companion.PieceType): List<Int> {

        val p = ChessRenderer.hashToLocation(position)
        val positions = when(pieceType) {
            BoardConfigurator.Companion.PieceType.PAWN -> generateForPawn(p, type)
            BoardConfigurator.Companion.PieceType.KNIGHT -> generateForKnight(p)
            BoardConfigurator.Companion.PieceType.KING -> generateForKing(p)
            BoardConfigurator.Companion.PieceType.QUEEN -> generateForQueen(p)
            BoardConfigurator.Companion.PieceType.BISHOP -> generateForBishop(p)
            BoardConfigurator.Companion.PieceType.ROOK -> generateForRook(p)
        }

        return positions
    }

    fun getDirection(src: Point, dest: Point): MoveDirections?{
        val dist = Point((dest.x - src.x), (dest.y - src.y))

        val col: Byte = when(dist.x.toInt()) {
            in -ChessRenderer.BOARD_COL_COUNT..-1 -> MoveDirections.LEFT_STRAIGHT.value
            in 1..ChessRenderer.BOARD_COL_COUNT -> MoveDirections.RIGHT_STRAIGHT.value
            else -> 0x0.toByte()
        }
        val row: Byte =  when(dist.y.toInt()) {
            in -ChessRenderer.BOARD_COL_COUNT..-1 -> MoveDirections.TOP_STRAIGHT.value
            in 1..ChessRenderer.BOARD_COL_COUNT -> MoveDirections.BOTTOM_STRAIGHT.value
            else -> 0x0.toByte()
        }

        if(dist.x.toInt() != 0 && dist.y.toInt() == 0){
            return MoveDirections.from(col)
        } else if(dist.x.toInt() == 0 && dist.y.toInt() != 0)
            return MoveDirections.from(row)
        else if(dist.x.toInt() != 0 && dist.y.toInt() != 0 && abs(dist.x.toInt()) == abs(dist.y.toInt())) {
            return MoveDirections.from(((row.toInt() or col.toInt()) and 0xFF).toByte())
        } else {
            return null
        }
    }


    fun generateDiagonals(p: Point, steps: Int, m: HashSet<MoveDirections>): List<Int> {

        var array =  arrayListOf<Int>()
        if(m.contains(MoveDirections.LEFT_TOP_DIAGONAL)) array.addAll(stepper(Point(-1f, -1f), p, steps))
        if(m.contains(MoveDirections.LEFT_BOTTOM_DIAGONAL)) array.addAll(stepper(Point(-1f, 1f), p, steps))
        if(m.contains(MoveDirections.RIGHT_TOP_DIAGONAL)) array.addAll(stepper(Point(1f, -1f), p, steps))
        if(m.contains(MoveDirections.RIGHT_BOTTOM_DIAGONAL)) array.addAll(stepper(Point(1f, 1f), p, steps))

        return array
    }

    fun generateStraight(p: Point, steps: Int, m: HashSet<MoveDirections>): List<Int> {

        var arrayList = mutableListOf<Int>()
        if(m.contains(MoveDirections.LEFT_STRAIGHT)) arrayList.addAll(stepper(Point(-1f, 0f), p, steps))
        if(m.contains(MoveDirections.RIGHT_STRAIGHT)) arrayList.addAll(stepper(Point(1f, 0f), p, steps))
        if(m.contains(MoveDirections.TOP_STRAIGHT)) arrayList.addAll(stepper(Point(0f, -1f), p, steps))
        if(m.contains(MoveDirections.BOTTOM_STRAIGHT)) arrayList.addAll(stepper(Point(0f, 1f), p, steps))

        return arrayList
    }

    fun stepper(s: Point, bg: Point, steps: Int): List<Int>{

        var positions = mutableListOf<Int>()


        for(i in 1..steps) {
            var cur = Point(bg.x + i * s.x, bg.y + i * s.y)
            if(isValidLocation(cur)) {
                positions.add(ChessRenderer.locationHash(cur))
            }
        }

        return positions
    }

    private fun generateForQueen(p: Point): List<Int> {
        var a = mutableListOf<Int>()
        a.addAll(generateDiagonals(p, ChessRenderer.BOARD_COL_COUNT, ALL_DIAGONALS))
        a.addAll(generateStraight(p, ChessRenderer.BOARD_COL_COUNT, ALL_STRAIGHT))
        return a
    }

    private fun generateForKing(p: Point): List<Int>{
        var h = mutableListOf<Int>()

        h.addAll(generateStraight(p, 1, ALL_STRAIGHT))
        h.addAll(generateDiagonals(p, 1, ALL_DIAGONALS))

        return h
    }

    private fun generateForBishop(p: Point): List<Int> {
        var h = mutableListOf<Int>()
        h.addAll(generateDiagonals(p, ChessRenderer.BOARD_COL_COUNT, ALL_DIAGONALS))
        return h
    }

    private fun generateForRook(p: Point): List<Int> {
        var h = mutableListOf<Int>()
        h.addAll(generateStraight(p, ChessRenderer.BOARD_COL_COUNT, ALL_STRAIGHT))
        return h
    }

    private fun generateForPawn(p: Point, type: BoardConfigurator.Companion.PieceColor): List<Int> {
        val directions =  when(type) {
            BoardConfigurator.Companion.PieceColor.BLACK -> hashSetOf(
                MoveDirections.LEFT_BOTTOM_DIAGONAL,
                MoveDirections.RIGHT_BOTTOM_DIAGONAL,
                MoveDirections.BOTTOM_STRAIGHT
            )
            BoardConfigurator.Companion.PieceColor.WHITE -> hashSetOf(
                MoveDirections.LEFT_TOP_DIAGONAL,
                MoveDirections.RIGHT_TOP_DIAGONAL,
                MoveDirections.TOP_STRAIGHT
            )
        }
        val h = mutableListOf<Int>()
        h.addAll(generateStraight(p, 1, directions))
        h.addAll(generateDiagonals(p, 1, directions))

        return h
    }

    private fun generateForKnight(p: Point): List<Int> {

        val dir = hashSetOf(
            MoveDirections.TOP_STRAIGHT,
            MoveDirections.BOTTOM_STRAIGHT
        )

        val h = mutableListOf<Int>()
        h.addAll(generateStraight(p, 2, dir))
        val positions = mutableListOf<Int>()
        for(i in 0..1) {

            val JUMP_COUNT = 2 * (2 - i)
            val b = 2 - i

            val cpTop = ChessRenderer.hashToLocation(positions[i])


            for(j in -b..b step JUMP_COUNT) {
                val nPoint = Point(cpTop.x + j, cpTop.y)
                if(isValidLocation(nPoint)) {
                    positions.add(ChessRenderer.locationHash(nPoint))
                }
            }

            val cpBottom = ChessRenderer.hashToLocation(positions[2 + i])

            for(j in -b..b step JUMP_COUNT) {
                val nPoint = Point(cpBottom.x + j, cpBottom.y)
                if(isValidLocation(nPoint)) {
                    positions.add(ChessRenderer.locationHash(nPoint))
                }
            }
        }

        return positions
    }

    private fun isValidLocation(p: Point): Boolean {
        return (p.x.toInt() in 0..ChessRenderer.BOARD_ROW_COUNT-1 && p.y.toInt() in 0..ChessRenderer.BOARD_COL_COUNT-1)
    }

}