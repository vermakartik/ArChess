package com.mch.arkoted

import android.widget.TextView

object StatusUpdater {

    fun updateStatus(t: TextView, message: String) {
        t.text = message
    }

}