package com.mch.arkoted.utils

import android.util.Log
import android.widget.Toast
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.mch.arkoted.GameActivity
import com.mch.arkoted.dtypes.Point
import kotlin.math.abs
import kotlin.math.floor

class ChessRenderer(val activity: GameActivity) {

    interface PieceTapListener {
        fun onPieceTapListener(p: Point, nd: Node, ntype: TapNodeType): Boolean
    }

    interface BoardTapListener {
        fun onBoardTapListener(p: Point): Boolean
    }

    var mapPiecesPosition = mutableMapOf<Int, Point>()
    var selectedPieceHashCode: Int =  NO_PIECE_SELECTED

    lateinit var boardNode: Node

    lateinit var nodeSelectable: Node
    lateinit var cube: ModelRenderable

    var selectedBox = Point(-1f, -1f)
    lateinit var nodeBoxSelected: Node
    lateinit var nodeBoxModelRenderable: ModelRenderable

    var pieceLocationhashSet = mutableSetOf<Int>()
    var pieceListener: PieceTapListener? = null
    var boardTapListener: BoardTapListener? = null

    init {
        val c =  Color()
        c.set(0.501f, 0.20f, 0.780f, 0.2f)
        MaterialFactory.makeTransparentWithColor(activity.applicationContext, c)
            .thenAccept {
                cube = ShapeFactory.makeCube(Vector3(
                    SQUARE_SIZE_DEFAULT,
                    2 * BOARD_HEIGHT,
                    SQUARE_SIZE_DEFAULT
                ), Vector3(0f,
                    BOARD_HEIGHT, 0f), it)
            }

        val cb = Color()
        cb.set(0.78f, 0.49f, 0.20f, 0.3f)
        MaterialFactory.makeTransparentWithColor(activity.applicationContext, cb)
            .thenAccept {
                nodeBoxModelRenderable = ShapeFactory.makeCube(Vector3(
                    SQUARE_SIZE_DEFAULT, 2 * BOARD_HEIGHT,
                    SQUARE_SIZE_DEFAULT
                ), Vector3(0f, 0f, 0f), it)
            }
    }

    fun createChessConfiguration(): Node {
        val nd = Node()
        nd.renderable = PiecesAndBoard.chessMap[PiecesAndBoard.BOARD]

        boardNode = nd

        boardNode.setOnTapListener {
            h, m ->
//            val boxloc = Point(h.point.x, h.point.z)
            val bn = boardNode.worldToLocalPoint(h.point)
            val point = locationToChessBox(Point(bn.x, bn.z))
//            if(pieceLocationhashSet.contains(locationHash(point))){
//                Toast.makeText(activity, "[Board Tap Listener]: Can't Place at: $point. Peace Already Exists", Toast.LENGTH_LONG).show()
//                return@setOnTapListener
//            }
            Toast.makeText(activity, "[Board Tap Listener]: Activitated at location - $point", Toast.LENGTH_LONG).show()

            if(!::nodeBoxSelected.isInitialized){
                nodeBoxSelected = Node()
                boardNode.addChild(nodeBoxSelected)
                nodeBoxSelected.renderable = nodeBoxModelRenderable
                nodeBoxSelected.setOnTapListener {
                    m, e ->
                    boardNode.removeChild(nodeBoxSelected)
                    pieceListener!!.onPieceTapListener(Point(0f, 0f), nodeBoxSelected, TapNodeType.SQUARE_SELECT)
                }
            } else {
                boardNode.addChild(nodeBoxSelected)
            }

            val shouldAccept = boardTapListener?.onBoardTapListener(point)
            if(shouldAccept == false) return@setOnTapListener

            val bcoords =  getBoxCenter(point)
            nodeBoxSelected.localPosition = Vector3(bcoords.x, 2 * BOARD_HEIGHT, bcoords.y)

        }

        initBoard(nd)

//      placed the chess Board
        return nd
    }

    fun initBoard(ndParent: Node){

        for(i in 0..BOARD_ROW_COUNT -1) {
            placePieceAtPosition(PiecesAndBoard.PAWN_BLACK, ndParent, Point(i.toFloat(), 1f))
            placePieceAtPosition(PiecesAndBoard.PAWN_WHITE, ndParent, Point(i.toFloat(), 6f))
            pieceLocationhashSet.add(locationHash(Point(i.toFloat(), 1f)))
            pieceLocationhashSet.add(locationHash(Point(i.toFloat(), 6f)))
        }

        placePieceAtPosition(PiecesAndBoard.KING_BLACK, ndParent, Point(4f, 0f))
        placePieceAtPosition(PiecesAndBoard.QUEEN_BLACK, ndParent, Point(3f, 0f))
        placePieceAtPosition(PiecesAndBoard.BISHOP_BLACK, ndParent, Point(2f, 0f))
        placePieceAtPosition(PiecesAndBoard.KNIGHT_BLACK, ndParent, Point(1f, 0f))
        placePieceAtPosition(PiecesAndBoard.ROOK_BLACK, ndParent, Point(0f, 0f))
        placePieceAtPosition(PiecesAndBoard.BISHOP_BLACK, ndParent, Point(5f, 0f))
        placePieceAtPosition(PiecesAndBoard.KNIGHT_BLACK, ndParent, Point(6f, 0f))
        placePieceAtPosition(PiecesAndBoard.ROOK_BLACK, ndParent, Point(7f, 0f))


        placePieceAtPosition(PiecesAndBoard.KING_WHITE, ndParent, Point(4f, 7f))
        placePieceAtPosition(PiecesAndBoard.QUEEN_WHITE, ndParent, Point(3f, 7f))
        placePieceAtPosition(PiecesAndBoard.BISHOP_WHITE, ndParent, Point(2f, 7f))
        placePieceAtPosition(PiecesAndBoard.KNIGHT_WHITE, ndParent, Point(1f, 7f))
        placePieceAtPosition(PiecesAndBoard.ROOK_WHITE, ndParent, Point(0f, 7f))
        placePieceAtPosition(PiecesAndBoard.BISHOP_WHITE, ndParent, Point(5f, 7f))
        placePieceAtPosition(PiecesAndBoard.KNIGHT_WHITE, ndParent, Point(6f, 7f))
        placePieceAtPosition(PiecesAndBoard.ROOK_WHITE, ndParent, Point(7f, 7f))

        for(i in 0..BOARD_ROW_COUNT -1) {
            pieceLocationhashSet.add(locationHash(Point(i.toFloat(), 0f)))
            pieceLocationhashSet.add(locationHash(Point(i.toFloat(), 7f)))
        }

    }

    fun onTapListener(nd: Node) {

        selectedPieceHashCode = nd.hashCode()
        val point = mapPiecesPosition[selectedPieceHashCode]

        Toast.makeText(activity.applicationContext, "Called on Tap Listener for Node ${selectedPieceHashCode}: ${point}", Toast.LENGTH_LONG).show()
        point?.let {
            val location = getBoxCenter(point)
            if(!::nodeSelectable.isInitialized) {
                nodeSelectable = Node()
                nodeSelectable.renderable = cube
                boardNode.addChild(nodeSelectable)
                nodeSelectable.setOnTapListener {
                    m, e ->
                    boardNode.removeChild(nodeSelectable)
                    pieceListener!!.onPieceTapListener(Point(0f, 0f), nodeSelectable, TapNodeType.PIECE_SELECT)
                }
            }else {
                boardNode.addChild(nodeSelectable)
            }

            val shouldAccept = pieceListener!!.onPieceTapListener(it, nd, TapNodeType.PIECE)
            if(!shouldAccept) return@let

            nodeSelectable.localPosition = Vector3(location.x, BOARD_HEIGHT, location.y)
        }

    }

    fun placePieceAtPosition(p: Int, parent: Node, point: Point) {
        val nd = Node()
        parent.addChild(nd)
        nd.setOnTapListener {
            h, m ->
            onTapListener(nd)
        }
        nd.renderable = PiecesAndBoard.chessMap[p]
        val position = Point(point.x, point.y)
        mapPiecesPosition[nd.hashCode()] = position
        val l = getBoxCenter(position)
        nd.localPosition = Vector3(l.x,
            BOARD_HEIGHT, l.y)
    }

    fun movePiece(src: Point, dest: Point, nd: Node) {
        if(pieceLocationhashSet.contains(locationHash(src))) {

            pieceLocationhashSet.remove(locationHash(src))

            if(pieceLocationhashSet.contains(locationHash(dest))) {
                var ndToRemove: Node? = null
                try {
                    ndToRemove = boardNode.children.first {
                        val p = it.localPosition
                        val gHash = locationHash(locationToChessBox(Point(p.x, p.z)))
                        val dHash = locationHash(dest)

                        Log.d("__BOARD_NODE_TAG__", "Comparison - ${gHash} <> ${dHash}")
                        gHash == dHash
                    }
                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                }
                ndToRemove?.let {
                    Log.d("__Chess Rendered__", "found Node $ndToRemove")
                    boardNode.removeChild(ndToRemove)
                    pieceLocationhashSet.remove(locationHash(dest))
                }
            }

            pieceLocationhashSet.add(locationHash(dest))
            val sHash = nd.hashCode()
            mapPiecesPosition[sHash] = dest
            val pos = getBoxCenter(dest)
            nd.localPosition = Vector3(pos.x, BOARD_HEIGHT, pos.y)

        }
    }

    fun removeNode(nd: Node) {
        boardNode.removeChild(nd)
    }

    fun getNodeAtLocation(dest: Point): Node{
        return boardNode.children.first {
            val p = it.localPosition
            val gHash = locationHash(locationToChessBox(Point(p.x, p.z)))
            val dHash = locationHash(dest)

            Log.d("__BOARD_NODE_TAG__", "Comparison - ${gHash} <> ${dHash}")
            gHash == dHash
        }
    }

    fun clearSelections(){
        boardNode.removeChild(nodeBoxSelected)
        boardNode.removeChild(nodeSelectable)
    }

    fun getTransformedOrigin(): Point{
        val d = (BOARD_ROW_COUNT * SQUARE_SIZE_DEFAULT) / 2
        return Point(-d, -d)
    }

    fun locationToChessBox(pos: Point): Point{
        val p = getTransformedOrigin()
        val transformer = Point(abs(p.x), abs(p.y))
        val locationNew = Point(pos.x + transformer.x, pos.y + transformer.y)

        val box = Point(floor(locationNew.x / SQUARE_SIZE_DEFAULT), floor(locationNew.y / SQUARE_SIZE_DEFAULT))
        return box
    }

    fun getBoxCenter(pos: Point): Point {
        val p = getTransformedOrigin()
        return Point(p.x + pos.x * SQUARE_SIZE_DEFAULT + SQUARE_SIZE_DEFAULT / 2 , p.y + pos.y * SQUARE_SIZE_DEFAULT + SQUARE_SIZE_DEFAULT / 2)
    }


    companion object {

        val BOARD_ROW_COUNT = 8
        val BOARD_COL_COUNT = 8
        val SQUARE_SIZE_DEFAULT = 0.0476f
        val BOARD_HEIGHT = 0.005f
        val NO_PIECE_SELECTED = 0x0
        val MAX_PIECE_HEIGHT = 0.07f

        enum class TapNodeType(val b: Byte) {
            PIECE(0x1),
            PIECE_SELECT(0x2),
            SQUARE_SELECT(0x4);

            companion object {
                fun from(b: Byte): TapNodeType { return values().first { it.b == b } }
            }
        }

        fun locationHash(p: Point): Int {
            return p.y.toInt() * BOARD_ROW_COUNT + p.x.toInt()
        }

        fun hashToLocation(h: Int): Point {
            return Point((h % BOARD_ROW_COUNT).toFloat(), (h / BOARD_ROW_COUNT).toFloat())
        }

    }
}