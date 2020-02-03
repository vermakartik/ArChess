package com.mch.arkoted.utils

import com.mch.arkoted.dtypes.Point
import com.mch.arkoted.dtypes.Square
import kotlin.math.abs
import kotlin.math.max

object RuleBook {

    val NULL_POINT = Point(-10f, 10f)

    fun checkMovePossible(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean{
        return when(src.type) {
            BoardConfigurator.Companion.PieceType.PAWN -> checkForPawn(src, dest, boardConfigurator, playerState)
            BoardConfigurator.Companion.PieceType.BISHOP -> checkForBishop(src, dest, boardConfigurator, playerState)
            BoardConfigurator.Companion.PieceType.KNIGHT -> checkForKnight(src, dest, boardConfigurator, playerState)
            BoardConfigurator.Companion.PieceType.ROOK -> checkForRook(src, dest, boardConfigurator, playerState)
            BoardConfigurator.Companion.PieceType.KING -> checkForKing(src, dest, boardConfigurator, playerState)
            BoardConfigurator.Companion.PieceType.QUEEN -> checkForQueen(src, dest, boardConfigurator, playerState)
            else -> false
        }
    }

    private fun checkForKnight(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {

        val dist = Point(abs(dest.point.x - src.point.x), abs(dest.point.y - src.point.y))
        return ((dist.x.toInt() == 2 && dist.y.toInt() == 1) || ((dist.x.toInt() == 1 && dist.y.toInt() == 2))) && checkDestPointEmptiness(dest, playerState, boardConfigurator) && !checkKingThreatOnMoveAnother(src, dest, playerState, boardConfigurator)

    }

    private fun checkForRook(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {
        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        return (direction != null) && (direction in PieceConfiguration.ALL_STRAIGHT) && !checkObstacleInPath(src.point, dest.point, boardConfigurator) && checkDestPointEmptiness(dest, playerState , boardConfigurator) && !checkKingThreatOnMoveAnother(src, dest, playerState, boardConfigurator)
    }

    private fun checkForBishop(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {
        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        return (direction != null) && (direction in PieceConfiguration.ALL_DIAGONALS) && !checkObstacleInPath(src.point, dest.point, boardConfigurator) && checkDestPointEmptiness(dest, playerState, boardConfigurator) && !checkKingThreatOnMoveAnother(src, dest, playerState, boardConfigurator)
    }

    private fun checkForKing(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {
        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        val dist = Point((dest.point.x - src.point.x), (dest.point.y - src.point.y))
        return (direction != null) &&
            (
                (abs(dist.x.toInt()) <= 1 && abs(dist.y.toInt()) <= 1 && ((abs(dist.x.toInt()) > 0 || abs(dist.y.toInt()) > 0)) &&
                    checkDestPointEmptiness(dest, playerState, boardConfigurator) &&
                    generateThreatsFromVicinity(dest, playerState, boardConfigurator).isEmpty()
                ) || checkIsCastling(src, dest, playerState, boardConfigurator)
            )
    }

    private fun checkForPawn(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {

        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        val dist = Point(dest.point.x - src.point.x, dest.point.y - src.point.y)
        return when(playerState.pieceColor) {
            BoardConfigurator.Companion.PieceColor.WHITE -> {
                when(direction) {
                    PieceConfiguration.MoveDirections.TOP_STRAIGHT -> (
                            (dist.x.toInt() == 0 && abs(dist.y.toInt()) == 1 && boardConfigurator.getPointInfo(dest.point).empty) ||
                            (dist.x.toInt() == 0 && abs(dist.y.toInt()) == 2 && boardConfigurator.getPointInfo(dest.point).empty && src.point.y.toInt() == 6)
                        )
                    PieceConfiguration.MoveDirections.LEFT_TOP_DIAGONAL, PieceConfiguration.MoveDirections.RIGHT_TOP_DIAGONAL ->
                        checkIsEnpassant(src, dest, playerState, boardConfigurator) != NULL_POINT
                    else -> false
                }
            }
            BoardConfigurator.Companion.PieceColor.BLACK -> {
                when(direction) {
                    PieceConfiguration.MoveDirections.BOTTOM_STRAIGHT ->
                        (dist.x.toInt() == 0 && dist.y.toInt() == 1 && boardConfigurator.getPointInfo(dest.point).empty) ||
                        (dist.x.toInt() == 0 && abs(dist.y.toInt()) == 2 && boardConfigurator.getPointInfo(dest.point).empty && src.point.y.toInt() == 1)
                    PieceConfiguration.MoveDirections.RIGHT_BOTTOM_DIAGONAL, PieceConfiguration.MoveDirections.LEFT_BOTTOM_DIAGONAL  ->
                        checkIsEnpassant(src, dest, playerState, boardConfigurator) != NULL_POINT
                    else -> false
                }
            }
        } && !checkKingThreatOnMoveAnother(src, dest, playerState, boardConfigurator)

    }


    private fun checkForQueen(src: Square, dest: Square, boardConfigurator: BoardConfigurator, playerState: PlayerState): Boolean {
        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        return (direction != null) && !checkObstacleInPath(src.point, dest.point, boardConfigurator) && checkDestPointEmptiness(dest, playerState, boardConfigurator) && !checkKingThreatOnMoveAnother(src, dest, playerState, boardConfigurator)
    }

    private fun checkObstacleInPath(src: Point, dest: Point, boardConfigurator: BoardConfigurator): Boolean{
        val d = Point((dest.x - src.x), (dest.y - src.y))
        val dir = PieceConfiguration.getDirection(src, dest)
        val stepCount: Int = max(abs(d.x.toInt()), abs(d.y.toInt()))

        lateinit var points: List<Int>
        if(dir in PieceConfiguration.ALL_DIAGONALS) {
            points = PieceConfiguration.generateDiagonals(src, stepCount, hashSetOf(dir as PieceConfiguration.MoveDirections))
        } else if(dir in PieceConfiguration.ALL_STRAIGHT) {
            points = PieceConfiguration.generateStraight(src, stepCount, hashSetOf(dir as PieceConfiguration.MoveDirections))
        }
        val destHash = ChessRenderer.locationHash(dest)
        var foundObstacle = false
        for(p in points) {
            if(p == destHash) {
                break
            } else {
                val info = boardConfigurator.getPointInfo(ChessRenderer.hashToLocation(p))
                if(!info.empty) {
                    foundObstacle = true
                    break
                }
            }
        }
        return foundObstacle
    }

    private fun checkDestPointEmptiness(dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Boolean {
        val dInfo = boardConfigurator.getPointInfo(dest.point)
        return (dInfo.empty) ||
            (
                dInfo.type != BoardConfigurator.Companion.PieceType.KING &&
                dInfo.color == BoardConfigurator.Companion.PieceColor.inverse(playerState.pieceColor)
            )
    }

    private fun checkKingThreatOnMoveAnother(src: Square, dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Boolean{
        boardConfigurator.movePiece(src.point, dest.point)
        val t = checkKingThreat(playerState, boardConfigurator)
        boardConfigurator.movePiece(dest.point, src.point)
        return t
    }

    private fun checkKingThreat(playerState: PlayerState, boardConfigurator: BoardConfigurator): Boolean {
        val kingLoc = boardConfigurator.getPointInfo(ChessRenderer.hashToLocation(boardConfigurator.getPieceLocation(playerState.pieceColor, BoardConfigurator.Companion.PieceType.KING)!!.first()))
        val threatList = generateThreatsFromVicinity(kingLoc, playerState, boardConfigurator)
        return threatList.isNotEmpty()
    }

    private fun generateThreatsFromVicinity(dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): List<Square> {
        var threatList = arrayListOf<Square>()

        val oColor = BoardConfigurator.Companion.PieceColor.inverse(playerState.pieceColor)

//        check Threat From Queen
        val pQueen = ChessRenderer.hashToLocation(boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.QUEEN)]!!.first())
        val pQueenDir = PieceConfiguration.getDirection(dest.point, pQueen)
        if(pQueenDir != null && !checkObstacleInPath(pQueen, dest.point, boardConfigurator)) threatList.add(Square(pQueen, oColor, BoardConfigurator.Companion.PieceType.QUEEN, false))

//        check Threat from King
        val pKing = ChessRenderer.hashToLocation(boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.KING)]!!.first())
        val pKingDir = PieceConfiguration.getDirection(dest.point, pKing)
        val distance = max(abs(dest.point.x - pKing.x).toInt(), abs(dest.point.y - pKing.y).toInt())
        if(pKingDir != null && distance <= 1) threatList.add(Square(pKing, oColor, BoardConfigurator.Companion.PieceType.KING, false))

//        check Threat for Bishop
        boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.BISHOP)]!!.let {
            for(p in it) {
                val pnt = ChessRenderer.hashToLocation(p)
                val dir = PieceConfiguration.getDirection(dest.point, pnt)
                if(dir != null && dir in PieceConfiguration.ALL_DIAGONALS && !checkObstacleInPath(pnt, dest.point, boardConfigurator)) threatList.add(Square(pnt, oColor, BoardConfigurator.Companion.PieceType.BISHOP, false))
            }
        }

//        check threats from rook
        boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.ROOK)]!!.let {
            for(p in it) {
                val pnt = ChessRenderer.hashToLocation(p)
                val dir = PieceConfiguration.getDirection(dest.point, pnt)
                if(dir != null && dir in PieceConfiguration.ALL_STRAIGHT && !checkObstacleInPath(pnt, dest.point, boardConfigurator)) threatList.add(Square(pnt, oColor, BoardConfigurator.Companion.PieceType.ROOK, false))
            }
        }

//        check For Knight
        boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.KNIGHT)]!!.let {
            for(p in it) {
                val pnt = ChessRenderer.hashToLocation(p)
                val dist = Point(abs(dest.point.x - pnt.x), abs(dest.point.y - pnt.y))
                if((dist.x.toInt() == 1 && dist.y.toInt() == 1) or (dist.x.toInt() == 2 && dist.y.toInt() == 1)) {
                   threatList.add(Square(pnt, oColor, BoardConfigurator.Companion.PieceType.KNIGHT, false))
                }
            }
        }

//        check For Pawn
        val pntsToCheck = when(oColor) {
            BoardConfigurator.Companion.PieceColor.BLACK ->
                arrayListOf(ChessRenderer.locationHash(Point(dest.point.x - 1, dest.point.y - 1)), ChessRenderer.locationHash(Point(dest.point.x + 1, dest.point.y - 1)))
            BoardConfigurator.Companion.PieceColor.WHITE ->
                arrayListOf<Int>(ChessRenderer.locationHash(Point(dest.point.x - 1, dest.point.y + 1)), ChessRenderer.locationHash(Point(dest.point.x + 1, dest.point.y + 1)))
        }

        val pntsAvailable = boardConfigurator.positionMap[BoardConfigurator.makePiece(oColor, BoardConfigurator.Companion.PieceType.PAWN)]

        pntsAvailable!!.let {
            for(pt in pntsToCheck) {
                if(it.contains(pt)) {
                    threatList.add(Square(ChessRenderer.hashToLocation(pt), oColor, BoardConfigurator.Companion.PieceType.PAWN, false))
                }
            }
        }

        return threatList
    }

    fun checkCastlingSuperficial(src: Square, dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): BoardConfigurator.CastlingSide {
        val dist = Point((dest.point.x - src.point.x), (dest.point.y - src.point.y))
        return BoardConfigurator.CastlingSide.from(dist.x.toInt())
    }

    fun checkIsCastling(src: Square, dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Boolean{
        val dist = Point((dest.point.x - src.point.x), (dest.point.y - src.point.y))
        return (abs(dist.x.toInt()) == 2 && abs(dist.y.toInt()) == 0) && checkCastlingPossible(src, dest, playerState, boardConfigurator)
    }

    fun checkCastlingPossible(src: Square, dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Boolean {
        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        val dist = Point((dest.point.x - src.point.x), (dest.point.y - src.point.y))

        return when(BoardConfigurator.CastlingSide.from(dist.x.toInt())){

             BoardConfigurator.CastlingSide.SHORT_SIDE -> {
                val rookLocation = Point((ChessRenderer.BOARD_COL_COUNT - 1).toFloat(), src.point.y)
                val pInfo = boardConfigurator.getPointInfo(rookLocation)
                generateThreatsFromVicinity(
                    Square(Point(src.point.x + 1, src.point.y), playerState.pieceColor, BoardConfigurator.Companion.PieceType.KING, false),
                    playerState,
                    boardConfigurator
                ).isEmpty() &&
                (pInfo.type == BoardConfigurator.Companion.PieceType.ROOK && pInfo.color == playerState.pieceColor) &&
                playerState.isCastlingPossible && !checkObstacleInPath(src.point, rookLocation, boardConfigurator)
            }

            BoardConfigurator.CastlingSide.LONG_SIDE -> {
                val rookLocation = Point(0f, src.point.y)
                val pInfo = boardConfigurator.getPointInfo(rookLocation)

                generateThreatsFromVicinity(
                    Square(Point(src.point.x - 1, src.point.y), playerState.pieceColor, BoardConfigurator.Companion.PieceType.KING, false),
                    playerState,
                    boardConfigurator
                ).isEmpty()
                (pInfo.type == BoardConfigurator.Companion.PieceType.ROOK && pInfo.color == playerState.pieceColor) &&
                playerState.isCastlingPossible && !checkObstacleInPath(src.point, rookLocation, boardConfigurator)
            }
        }
    }

    fun checkIsEnpassant(src: Square, dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Point{

        val direction = PieceConfiguration.getDirection(src.point, dest.point)
        val dist = Point(dest.point.x - src.point.x, dest.point.y - src.point.y)

        if(!playerState.isPawnPassantEnabled) return NULL_POINT

        return when(playerState.pieceColor) {
            BoardConfigurator.Companion.PieceColor.WHITE -> {
                when(direction) {
                    PieceConfiguration.MoveDirections.RIGHT_TOP_DIAGONAL -> {
                        val st = boardConfigurator.getPointInfo(dest.point)
                        val isPossible = (
                            dist.x.toInt() == 1 &&
                            dist.y.toInt() == -1 &&
                            ((!st.empty && st.color == BoardConfigurator.Companion.PieceColor.BLACK) ||
                                (
                                    playerState.isPawnPassantEnabled &&
                                    playerState.enPassantPieceInfo == Point(dest.point.x, dest.point.y + 1) &&
                                    boardConfigurator.getPointInfo(Point(src.point.x + 1, src.point.y)).color == BoardConfigurator.Companion.PieceColor.BLACK &&
                                    boardConfigurator.getPointInfo(Point(src.point.x + 1, src.point.y)).type == BoardConfigurator.Companion.PieceType.PAWN
                                )
                            ))
                        return  when(isPossible) {
                            true -> Point(dest.point.x, dest.point.y - 1)
                            false -> NULL_POINT
                        }
                    }
                    PieceConfiguration.MoveDirections.LEFT_TOP_DIAGONAL -> {
                        val st = boardConfigurator.getPointInfo(dest.point)
                        val isPossible = (dist.x.toInt() == -1 && dist.y.toInt() == -1 &&
                        ((!st.empty && st.color == BoardConfigurator.Companion.PieceColor.BLACK) ||
                            (
                                playerState.isPawnPassantEnabled &&
                                playerState.enPassantPieceInfo == Point(dest.point.x, dest.point.y + 1) &&
                                boardConfigurator.getPointInfo(Point(src.point.x - 1, src.point.y)).color == BoardConfigurator.Companion.PieceColor.BLACK &&
                                boardConfigurator.getPointInfo(Point(src.point.x - 1, src.point.y)).type == BoardConfigurator.Companion.PieceType.PAWN
                            )
                        ))
                        return  when(isPossible) {
                            true -> Point(dest.point.x, dest.point.y - 1)
                            false -> NULL_POINT
                        }
                    }
                    else -> NULL_POINT
                }
            }
            BoardConfigurator.Companion.PieceColor.BLACK -> {
                when(direction) {
                    PieceConfiguration.MoveDirections.RIGHT_BOTTOM_DIAGONAL -> {
                        val st = boardConfigurator.getPointInfo(dest.point)
                        val isPossible = (
                            dist.x.toInt() == 1 && dist.y.toInt() == 1 &&
                            ((!st.empty && st.color == BoardConfigurator.Companion.PieceColor.WHITE) ||
                            (
                                playerState.isPawnPassantEnabled &&
                                playerState.enPassantPieceInfo == Point(dest.point.x, dest.point.y - 1) &&
                                boardConfigurator.getPointInfo(Point(src.point.x + 1, src.point.y)).color == BoardConfigurator.Companion.PieceColor.WHITE &&
                                boardConfigurator.getPointInfo(Point(src.point.x + 1, src.point.y)).type == BoardConfigurator.Companion.PieceType.PAWN
                            ))
                        )
                        return  when(isPossible) {
                            true -> Point(dest.point.x, dest.point.y + 1)
                            false -> NULL_POINT
                        }
                    }
                    PieceConfiguration.MoveDirections.LEFT_BOTTOM_DIAGONAL -> {
                        val st = boardConfigurator.getPointInfo(dest.point)
                        val isPossible = (
                            dist.x.toInt() == -1 && dist.y.toInt() == 1 &&
                            ((!st.empty && st.color == BoardConfigurator.Companion.PieceColor.WHITE) ||
                             (
                                playerState.isPawnPassantEnabled &&
                                playerState.enPassantPieceInfo == Point(dest.point.x, dest.point.y - 1) &&
                                boardConfigurator.getPointInfo(Point(src.point.x - 1, src.point.y)).color == BoardConfigurator.Companion.PieceColor.WHITE &&
                                boardConfigurator.getPointInfo(Point(src.point.x - 1, src.point.y)).type == BoardConfigurator.Companion.PieceType.PAWN
                            ))
                        )
                        return  when(isPossible) {
                            true -> Point(dest.point.x, dest.point.y + 1)
                            false -> NULL_POINT
                        }
                    }
                    else -> NULL_POINT
                }
            }
        }
    }

    fun shouldEnableEnPassant(dest: Square, playerState: PlayerState, boardConfigurator: BoardConfigurator): Point{
        val pos = PieceConfiguration.generateStraight(dest.point, 1, hashSetOf(PieceConfiguration.MoveDirections.LEFT_STRAIGHT, PieceConfiguration.MoveDirections.RIGHT_STRAIGHT))
        val oColor = BoardConfigurator.Companion.PieceColor.inverse(playerState.pieceColor)
        for(p in pos) {
            val pnt = ChessRenderer.hashToLocation(p)
            val info = boardConfigurator.getPointInfo(pnt)
            if(info.type == BoardConfigurator.Companion.PieceType.PAWN && info.color == oColor && when(playerState.pieceColor) {
                    BoardConfigurator.Companion.PieceColor.WHITE -> dest.point.y.toInt() == 4
                    BoardConfigurator.Companion.PieceColor.BLACK -> dest.point.y.toInt() == 3
                }) {
                return pnt
            }
        }
        return NULL_POINT
    }
}