package com.example.mynote.activity

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

class CrashlyticsLogger : Timber.Tree() {
    private val crashlytics by lazy { Firebase.crashlytics }

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        t ?: return

        crashlytics.run {
            setCustomKey(
                "priority",
                when (priority) {
                    Log.INFO -> "Log.INFO"
                    Log.WARN -> "Log.WARN"
                    Log.ERROR -> "Log.ERROR"
                    Log.ASSERT -> "Log.ASSERT"
                    else -> "Log.$priority"
                }
            )
            setCustomKey("tag", tag.orEmpty())
            setCustomKey("message", message)
            setCustomKey("throwable_message", t.message.orEmpty())

            log("[$tag]: $message")
            recordException(t)
        }
    }
}