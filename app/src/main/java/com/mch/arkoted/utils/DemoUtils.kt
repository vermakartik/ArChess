package com.mch.arkoted.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.mch.arkoted.GameActivity


object DemoUtils {

    val MIN_OPEN_GL_VERSION = 3.0

    fun createArSessionWithoutInstall(activity: GameActivity, lightEstimate: Config.LightEstimationMode): Session?{
        return createARSession(
            activity,
            lightEstimate,
            true
        )
    }

    fun createArSessionInstallRequest(activity: GameActivity, lightEstimate: Config.LightEstimationMode): Session?{
        return createARSession(
            activity,
            lightEstimate,
            false
        )
    }

    fun createARSession(activity: GameActivity, lightEstimate: Config.LightEstimationMode, requestInstall: Boolean): Session? {
        var session: Session? = null
        if(hasCameraPermission(activity)) {
            when(ArCoreApk.getInstance().requestInstall(activity, !requestInstall)){
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return null
                ArCoreApk.InstallStatus.INSTALLED -> Log.d("__DEMO_UTILS__", "Requesting Apk Install")
            }

            session = Session(activity)
            val config = Config(session)
            config.lightEstimationMode = lightEstimate
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }

    fun hasCameraPermission(activity: GameActivity): Boolean {
        return ContextCompat.checkSelfPermission(activity.applicationContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: GameActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA),
            GameActivity.RC_PERMISSION
        )
    }

    fun shouldShowPermissionRationale(activity: GameActivity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)
    }

    fun launchPermissionSettings(activity: GameActivity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    fun checkIsDeviceSupported(activity: GameActivity): Boolean {
        Log.d("__DEMO_UTILS__", "Starting checkIsDeviceSupported")
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(activity, "AR Requires Android Version N or Higher. Can't run here!", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }

        val openGLVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.glEsVersion

        if(openGLVersionString.toDouble() < MIN_OPEN_GL_VERSION) {
            Toast.makeText(activity, "Open GL Version required 3.0", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        Log.d("__DEMO_UTILS__", "Ending checkIsDeviceSupported")

        return true
    }

 }