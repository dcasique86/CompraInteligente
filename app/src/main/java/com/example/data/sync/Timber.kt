package com.example.data.sync

import android.util.Log
import timber.log.Timber

object Timber : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE) return
        super.log(priority, tag, message, t)
    }
}