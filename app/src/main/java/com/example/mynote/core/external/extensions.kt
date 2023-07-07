package com.example.mynote.core.external

import android.os.Looper
import androidx.navigation.NavController
import timber.log.Timber

/**
 * Performs a navigation on the [NavController] using the provided [block],
 * catching any [IllegalArgumentException] which usually happens when users trigger (e.g. click)
 * navigation multiple times very quickly on slower devices.
 * For more context, see [stackoverflow](https://stackoverflow.com/questions/51060762/illegalargumentexception-navigation-destination-xxx-is-unknown-to-this-navcontr).
 */
fun NavController.safeNavigate(block: NavController.() -> Unit) {
  try {
    this.block()
  } catch (e: IllegalArgumentException) {
    Timber.e(e, "Handled navigation destination not found issue gracefully.")
  }
}

fun checkMainThread() {
  check(Looper.getMainLooper() === Looper.myLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
  }
}