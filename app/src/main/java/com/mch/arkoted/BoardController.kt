package com.mch.arkoted

import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.mch.arkoted.utils.BoardConfigurator
import com.mch.arkoted.utils.ChessRenderer
import com.mch.arkoted.dtypes.Point
import com.mch.arkoted.exceptions.PieceColorException
import com.mch.arkoted.exceptions.PieceTypeNotFoundException
import com.mch.arkoted.utils.PlayerState
import com.mch.arkoted.utils.RuleBook

class BoardController private constructor(val activity: GameActivity) {

    interface Watcher{
        fun onChange(e: Boolean)
    }

    var isPlaced = false
    lateinit var chessRenderer: ChessRenderer
    lateinit var boardConfigurator: BoardConfigurator

    var piecePoint: Point? = null
    var boardSquarePoint: Point? = null
    var nodePiece: Node? = null
    lateinit var watcher: Watcher

    var isPieceSelected = false
    var isBoardSquareSelected = false

    fun placeBoard(sceneView: ArSceneView, e: MotionEvent?){

        val f = sceneView.arFrame

        f?.let { frame ->
            val hits = frame.hitTest(e)
            for(h in hits) {
                val t = h.trackable
                if(t is Plane && t.isPoseInPolygon(h.hitPose)) {

                    if(isPlaced) continue

                    val anchor = h.createAnchor()
                    val anchorNode = AnchorNode(anchor)

                    anchorNode.setParent(sceneView.scene)

                    chessRenderer = ChessRenderer(activity)
                    boardConfigurator = BoardConfigurator()

                    val nd = chessRenderer.createChessConfiguration()
                    boardConfigurator.initBoard()
                    isPlaced = true

                    chessRenderer.pieceListener = object : ChessRenderer.PieceTapListener{
                        override fun onPieceTapListener(p: Point, nd: Node, ntype: ChessRenderer.Companion.TapNodeType): Boolean {
                            return when(ntype) {
                                ChessRenderer.Companion.TapNodeType.PIECE -> {
                                    Toast.makeText(activity.applicationContext, "Clicked on piece: $p", Toast.LENGTH_LONG).show()
                                    piecePoint = p
                                    nodePiece = nd
                                    watcher.onChange(piecePoint != null && boardSquarePoint != null)
                                    true
                                }
                                ChessRenderer.Companion.TapNodeType.PIECE_SELECT -> {
                                    isPieceSelected = false
                                    piecePoint = null
                                    true
                                }
                                ChessRenderer.Companion.TapNodeType.SQUARE_SELECT -> {
                                    isBoardSquareSelected = false
                                    boardSquarePoint = null
                                    true
                                }

                            }
                        }
                    }

                    chessRenderer.boardTapListener = object : ChessRenderer.BoardTapListener {
                        override fun onBoardTapListener(p: Point): Boolean {
                            Toast.makeText(activity.applicationContext, "Clicked on Board: $p", Toast.LENGTH_LONG).show()
                            boardSquarePoint = p
                            watcher.onChange(piecePoint != null && boardSquarePoint != null)
                            return true
                        }
                    }

                    anchorNode.addChild(nd)
                }
            }
        }

    }

    fun movePieceToPosition(){

        if(piecePoint != null && boardSquarePoint != null) {

            val pPoint = (piecePoint as Point)
            val bPoint = (boardSquarePoint as Point)

            val pSquare = boardConfigurator.getPointInfo(pPoint)
            val bSquare = boardConfigurator.getPointInfo(bPoint)

            val ps: PlayerState = getPlayerStateFromColor(pSquare.color)

            if(RuleBook.checkMovePossible(pSquare, bSquare, boardConfigurator, ps)) {

                if(pSquare.type == BoardConfigurator.Companion.PieceType.KING) {
                    try {
                        val iC = RuleBook.checkCastlingSuperficial(pSquare, bSquare, ps, boardConfigurator)
                        when(iC) {
                            BoardConfigurator.CastlingSide.LONG_SIDE -> {

                                val rookLocation = Point(0f, pPoint.y)

                                boardConfigurator.movePiece(pSquare.point, bSquare.point)
                                boardConfigurator.movePiece(rookLocation, Point((pPoint.x - 1), pPoint.y))

                                chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)
                                chessRenderer.movePiece(rookLocation, Point((pPoint.x - 1), pPoint.y), chessRenderer.getNodeAtLocation(rookLocation))

                            }
                            BoardConfigurator.CastlingSide.SHORT_SIDE -> {

                                val rookLocation = Point(7f, pPoint.y)

                                boardConfigurator.movePiece(pSquare.point, bSquare.point)
                                boardConfigurator.movePiece(rookLocation, Point((pPoint.x + 1), pPoint.y))

                                chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)
                                chessRenderer.movePiece(rookLocation, Point((pPoint.x + 1), pPoint.y), chessRenderer.getNodeAtLocation(rookLocation))

                            }
                        }
                        ps.isCastlingPossible = false
                    } catch (e: NoSuchElementException) {
                        boardConfigurator.movePiece(pSquare.point, bSquare.point)
                        chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)
                    }
                    ps.isPawnPassantEnabled = false
                    ps.enPassantPieceInfo = null

                } else if(pSquare.type == BoardConfigurator.Companion.PieceType.PAWN) {
                    val bc = RuleBook.checkIsEnpassant(pSquare, bSquare, ps, boardConfigurator)
                    when(bc) {
                        RuleBook.NULL_POINT -> {
                            boardConfigurator.movePiece(pSquare.point, bSquare.point)
                            chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)

                            val pfe = RuleBook.shouldEnableEnPassant(bSquare, ps, boardConfigurator)
                            if(pfe != RuleBook.NULL_POINT) {
                                val psfe = getPlayerStateFromColor(BoardConfigurator.Companion.PieceColor.inverse(pSquare.color as BoardConfigurator.Companion.PieceColor))
                                psfe.isPawnPassantEnabled = true
                                psfe.enPassantPieceInfo = pfe
                            }
                        }
                        else -> {
                            boardConfigurator.movePiece(pSquare.point, bSquare.point)
                            chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)
                            chessRenderer.removeNode(chessRenderer.getNodeAtLocation(bc))
                        }
                    }
                }
                else {
                    boardConfigurator.movePiece(pSquare.point, bSquare.point)
                    chessRenderer.movePiece(pSquare.point, bSquare.point, nodePiece as Node)

                    ps.isPawnPassantEnabled = false
                    ps.enPassantPieceInfo = null
                }

                clearSelections()

            }
        }
    }

    fun getPlayerStateFromColor(pieceColor: BoardConfigurator.Companion.PieceColor?): PlayerState {
        return when(pieceColor)  {
            BoardConfigurator.Companion.PieceColor.WHITE -> boardConfigurator.whitePlayerState
            BoardConfigurator.Companion.PieceColor.BLACK -> boardConfigurator.blackPlayerState
            else -> throw PieceColorException()
        }
    }

    fun clearSelections(){

        piecePoint = null
        boardSquarePoint = null

        isPieceSelected = false
        isBoardSquareSelected = false

        chessRenderer.clearSelections()
    }

    fun checkIsEnabled(): Boolean{
        return (piecePoint != null && boardSquarePoint != null)
    }

    companion object  {
        var gInstance: BoardController? = null

        fun getInstance(activity: GameActivity): BoardController {
            lateinit var temp: BoardController
            if(gInstance != null) {
                temp = (gInstance as BoardController)
            } else {
                temp = BoardController(activity)
                gInstance = temp
            }
            return temp
        }
    }

}