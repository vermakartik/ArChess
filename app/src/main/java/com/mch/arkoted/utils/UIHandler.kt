package com.mch.arkoted.utils

import android.graphics.Point
import android.view.View

object UIHandler {
    fun getScreenCenter(view: View): Point {
        return Point(view.width / 2, view.height / 2)
    }
}