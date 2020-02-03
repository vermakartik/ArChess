package com.mch.arkoted

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView
import com.mch.arkoted.utils.*

class GameActivity : AppCompatActivity() {

    lateinit var arScene: ArSceneView
    lateinit var statusView: TextView

    var cameraPermissonRequested = false

    lateinit var loader: AssetLoader
    lateinit var gestureDetector: GestureDetector

    lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if(!DemoUtils.checkIsDeviceSupported(this)){
            finish()
        }

        arScene = findViewById(R.id.ar_main_scene)
        loader = AssetLoader(this)
        statusView = findViewById(R.id.tv_desc_status)
        imgView = findViewById(R.id.iv_frame_state)

        PiecesAndBoard.loadAllPieces(loader, this)

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                onSingleTap(e)
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }
        })

        arScene.scene.addOnUpdateListener {
            arScene?.arFrame?.let {
                updatePlaceConfiguration()
            }
        }

        arScene.scene.setOnTouchListener {
            hitTestResult, motionEvent -> gestureDetector.onTouchEvent(motionEvent)
        }

        DemoUtils.requestCameraPermission(this)

    }

    fun updatePlaceConfiguration(){
        val f = arScene.arFrame
        val c = UIHandler.getScreenCenter(arScene)
        val hits = f?.hitTest(c.x.toFloat(), c.y.toFloat())
        hits?.any {
            val t = it.trackable
            val isTracking = (t is Plane && t.isPoseInPolygon(it.hitPose))

            if(isTracking) {
                StatusUpdater.updateStatus(statusView, "Tracking Enabled!")
                findViewById<ImageView>(R.id.iv_frame_state).setImageDrawable(resources.getDrawable(R.drawable.target_found, applicationContext.theme))
            } else {
                StatusUpdater.updateStatus(statusView, "Tracking Disabled!")
                findViewById<ImageView>(R.id.iv_frame_state).setImageDrawable(resources.getDrawable(R.drawable.target_blank, applicationContext.theme))
            }

            isTracking
        }
    }


    fun onSingleTap(e: MotionEvent?) {
        Log.d(TAG, "__MOTION EVENT__: $e")
        Toast.makeText(this, "Hit at Location: ${e.toString()}", Toast.LENGTH_LONG).show()

        if(!BoardController.getInstance(this).isPlaced) {
            BoardController.getInstance(this).watcher = object : BoardController.Watcher{
                override fun onChange(e: Boolean) {
                    findViewById<TextView>(R.id.btn_move).setBackgroundColor(when(e) {
                        true -> Color.parseColor("#66d9d214")
                        false -> Color.parseColor("#66596214")
                    })
                }
            }
            BoardController.getInstance(this).placeBoard(arScene, e)
            findViewById<TextView>(R.id.btn_move).setOnClickListener {
                BoardController.getInstance(this).movePieceToPosition()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!DemoUtils.hasCameraPermission(this)){
            if(!DemoUtils.shouldShowPermissionRationale(this)){
                DemoUtils.launchPermissionSettings(this)
            } else {
                Toast.makeText(this, "Camera Permission is Needed to run this Application", Toast.LENGTH_LONG).show()

            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onResume() {
        super.onResume()

        arScene.let {
            if(it.session == null) {
                try {

                    val lightEstimationMode: Config.LightEstimationMode = Config.LightEstimationMode.DISABLED
                    val session = if(cameraPermissonRequested) DemoUtils.createArSessionInstallRequest(this, lightEstimationMode)  else
                        DemoUtils.createArSessionWithoutInstall(this, lightEstimationMode)
                    if(session == null) {
                        cameraPermissonRequested = DemoUtils.hasCameraPermission(this)
                        return
                    } else {
                        arScene.setupSession(session)
                    }

                } catch (e: UnavailableException) {
                    e.printStackTrace()
                    Toast.makeText(this , "Error $e", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Error $e")
                }
            }
        }

        try {
            arScene.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            Toast.makeText(this , "Error $e", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Error $e")
            finish()
            return
        }

        arScene.session?.let {
            StatusUpdater.updateStatus(statusView,  "Searching For Surfaces...")
        }

    }

    override fun onPause() {
        super.onPause()
        arScene.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arScene.destroy()
    }

    companion object {
        val RC_PERMISSION = 0x231
        val TAG = "__GAME_ACTIVITY__"
    }
}

