package com.example.mynote.base

import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlinx.coroutines.channels.Channel
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {
  /**
   * No need to maintain the insertion order -> uses [HashSet].
   */
  private val channelsBagLazy = lazy(NONE) { HashSet<Channel<*>>(1 shl 2 /*aka 4*/) }

  @get:MainThread
  private val channelsBag: MutableSet<Channel<*>> by channelsBagLazy

  protected open val logTag: String by lazy(PUBLICATION) {
    // Tag length limit was removed in API 26.
    this::class.java.simpleName
  }

  /**
   * Add [this] channel to the `bag` to close when [onCleared] is called.
   */
  @MainThread
  protected fun <T> Channel<T>.addToBag(): Channel<T> = apply { channelsBag += this }

  @CallSuper
  override fun onCleared() {
    super.onCleared()
    Timber.tag(logTag).d("onCleared $this")

    if (channelsBagLazy.isInitialized()) {
      Timber.tag(logTag).d("onCleared close $channelsBag")
      channelsBag.forEach { it.close() }
      channelsBag.clear()
    }
  }

  val timeSubReloadData = mutableMapOf<String?, Long>()
}
