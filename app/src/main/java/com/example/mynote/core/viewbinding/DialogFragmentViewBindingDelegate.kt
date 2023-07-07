package com.example.mynote.core.viewbinding

import android.app.Dialog
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.example.mynote.core.viewbinding.ViewBindingDialogFragment.OnDestroyViewListeners
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DialogFragmentViewBindingDelegate<T : ViewBinding, DF> private constructor(
  private val fragment: DF,
  @IdRes private val rootId: Int,
  viewBindingBind: ((View) -> T)? = null,
  viewBindingClazz: Class<T>? = null,
  private var onDestroyView: (T.() -> Unit)?
) : ReadOnlyProperty<DialogFragment, T> where DF : DialogFragment, DF : ViewBindingDialogFragment {

  private var binding: T? = null
  private val bind = viewBindingBind ?: { view: View ->
    @Suppress("UNCHECKED_CAST")
    GetBindMethod(viewBindingClazz!!)(null, view) as T
  }

  init {
    ensureMainThread()
    require(viewBindingBind != null || viewBindingClazz != null) {
      "Both viewBindingBind and viewBindingClazz are null. Please provide at least one."
    }

    fragment.lifecycle.addObserver(FragmentLifecycleObserver())
  }

  override fun getValue(thisRef: DialogFragment, property: KProperty<*>): T {
    return binding
      ?: thisRef.requireDialog().let { dialog ->
        bind(
          checkNotNull(dialog.findViewById(rootId)) {
            "ID $rootId does not reference a View inside this Dialog $dialog"
          }
        ).also { binding = it }
      }
  }

  private inner class FragmentLifecycleObserver : DefaultLifecycleObserver {
    val observer = Observer<OnDestroyViewListeners?> { listeners: OnDestroyViewListeners? ->
      listeners ?: return@Observer

      var onDestroyViewActual = onDestroyView
      listeners += {
        binding?.let { onDestroyViewActual?.invoke(it) }
        onDestroyViewActual = null
        binding = null
      }
    }

    override fun onCreate(owner: LifecycleOwner) {
      fragment.onDestroyViewLiveData.observeForever(observer)
    }

    override fun onDestroy(owner: LifecycleOwner) {
      fragment.lifecycle.removeObserver(this)
      fragment.onDestroyViewLiveData.removeObserver(observer)

      binding = null
      onDestroyView = null
    }
  }

  companion object Factory {
    /**
     * Create [DialogFragmentViewBindingDelegate] from [viewBindingBind] lambda function.
     *
     * @param viewBindingBind a lambda function that creates a [ViewBinding] instance from root view of the [Dialog] eg: `T::bind` static method can be used.
     */
    @JvmStatic
    fun <T : ViewBinding, DF> from(
      fragment: DF,
      @IdRes rootId: Int,
      viewBindingBind: (View) -> T,
      onDestroyView: (T.() -> Unit)?
    ): DialogFragmentViewBindingDelegate<T, DF> where DF : DialogFragment, DF : ViewBindingDialogFragment =
      DialogFragmentViewBindingDelegate(
        fragment = fragment,
        viewBindingBind = viewBindingBind,
        rootId = rootId,
        onDestroyView = onDestroyView
      )

    /**
     * Create [DialogFragmentViewBindingDelegate] from [viewBindingClazz] class.
     *
     * @param viewBindingClazz Kotlin Reflection will be used to get `T::bind` static method from this class.
     */
    @JvmStatic
    fun <T : ViewBinding, DF> from(
      fragment: DF,
      @IdRes rootId: Int,
      viewBindingClazz: Class<T>,
      onDestroyView: (T.() -> Unit)?
    ): DialogFragmentViewBindingDelegate<T, DF> where DF : DialogFragment, DF : ViewBindingDialogFragment =
      DialogFragmentViewBindingDelegate(
        fragment = fragment,
        viewBindingClazz = viewBindingClazz,
        rootId = rootId,
        onDestroyView = onDestroyView
      )
  }
}
