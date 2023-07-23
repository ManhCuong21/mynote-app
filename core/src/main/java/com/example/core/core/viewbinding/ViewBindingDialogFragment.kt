package com.example.core.core.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.CopyOnWriteArraySet

/**
 * An interface allows you to use [dialogFragmentViewBinding] and [DialogFragmentViewBindingDelegate].
 *
 * It is recommended to extend [DefaultViewBindingDialogFragment] before implementing this interface.
 * If not, you can copy/paste [DefaultViewBindingDialogFragment]'s implementation to make your own implementation.
 */
interface ViewBindingDialogFragment {
  /**
   * A [LiveData] which allows you to observe [OnDestroyViewListeners].
   *
   * This will be set to the new [OnDestroyViewListeners] when [DialogFragment.onCreateView] called
   * and will set to null when [DialogFragment.onDestroyView] called.
   *
   * The [OnDestroyViewListeners] will be invoked and disposed when when [DialogFragment.onDestroyView] called.
   */
  val onDestroyViewLiveData: LiveData<OnDestroyViewListeners?>

  /**
   * The class containing listeners will be invoked when [DialogFragment.onDestroyView] called.
   */
  @MainThread
  class OnDestroyViewListeners {
    private var isDisposed = false
    private val listeners: MutableSet<() -> Unit> = CopyOnWriteArraySet()

    operator fun plusAssign(listener: () -> Unit) {
      check(!isDisposed) { "Already disposed" }

      listeners += listener
    }

    operator fun invoke() {
      check(!isDisposed) { "Already disposed" }
      listeners.forEach { it() }
    }

    /**
     * Dispose this listeners. Should call once.
     */
    fun dispose() {
      check(!isDisposed) { "Already disposed" }
      listeners.clear()
      isDisposed = true
    }
  }
}

/**
 * Default implementation of [ViewBindingDialogFragment].
 * Extends this class to able to use [dialogFragmentViewBinding] and [DialogFragmentViewBindingDelegate]
 */
open class DefaultViewBindingDialogFragment : DialogFragment(), ViewBindingDialogFragment {
  protected open val logTag: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
    // Tag length limit was removed in API 26.
    this::class.java.simpleName
  }
  private var listeners: ViewBindingDialogFragment.OnDestroyViewListeners? = null
  private val _onDestroyViewLiveData = MutableLiveData<ViewBindingDialogFragment.OnDestroyViewListeners?>()

  final override val onDestroyViewLiveData: LiveData<ViewBindingDialogFragment.OnDestroyViewListeners?> get() = _onDestroyViewLiveData

  @CallSuper
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    println("$logTag onCreate $this")
    _onDestroyViewLiveData.value = ViewBindingDialogFragment.OnDestroyViewListeners()
      .also { listeners = it }
    return null
  }

  @CallSuper
  override fun onDestroyView() {
    super.onDestroyView()

    listeners?.run {
      invoke()
      dispose()
    }
    listeners = null
    _onDestroyViewLiveData.value = null
  }
}
