package com.example.core.core.viewbinding

import android.os.SystemClock
import kotlin.math.abs

object AvoidRapidAction {
  internal const val DEFAULT_DELAY_TIME = 300 // ms
  internal const val LONG_DELAY_TIME = 500 // ms
  internal const val SUPER_LONG_DELAY_TIME = 800 // ms
  private var lastClickTime: Long = 0

  internal fun action(timeDelay: Int = DEFAULT_DELAY_TIME, action: () -> Unit) {
    if (abs(SystemClock.elapsedRealtime() - lastClickTime) > timeDelay) {
      action.invoke()
      lastClickTime = SystemClock.elapsedRealtime()
    }
  }
}
