package com.example.core.core.viewbinding

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T : ViewBinding> private constructor(
  private val fragment: Fragment,
  viewBindingBind: ((View) -> T)? = null,
  viewBindingClazz: Class<T>? = null,
  private var onDestroyView: (T.() -> Unit)?
) : ReadOnlyProperty<Fragment, T> {

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

  override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
    binding?.let {
      if (it.root === thisRef.view) {
        return it
      } else {
        binding = null
      }
    }

    check(fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
      """Attempt to get view binding when fragment view is destroyed.
        |
        |We cannot access ViewBinding delegate property in onDestroyView(). Recommended way is passing a lambda to `onDestroyView: (T.() -> Unit)? = null` parameter of extension functions, eg.
        |
        |private val binding by viewBinding<FragmentFirstBinding> { /*this: FragmentFirstBinding*/
        |  button.setOnClickListener(null)
        |  recyclerView.adapter = null
        |}
        |
      """.trimMargin()
    }

    return bind(
      checkNotNull(thisRef.view) {
        """Fragment $thisRef did not return a View from onCreateView() or this was called before onCreateView().
          |Fragment's view must be not null before access `ViewBinding` property. This can be done easily with constructor:
          |
          |public androidx.fragment.app.Fragment(@LayoutRes int contentLayoutId) { ... }
          |
          |eg.
          |
          |class FirstFragment : Fragment(R.layout.fragment_first) { ... }
          |
        """.trimMargin()
      }
    ).also { binding = it }
  }

  private inner class FragmentLifecycleObserver : DefaultLifecycleObserver {
    val observer = Observer { viewLifecycleOwner: LifecycleOwner? ->
      viewLifecycleOwner ?: return@Observer

      var onDestroyViewActual = onDestroyView

      val viewLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
          viewLifecycleOwner.lifecycle.removeObserver(this)

          binding?.let { onDestroyViewActual?.invoke(it) }
          onDestroyViewActual = null
          binding = null
        }
      }
      viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)
    }

    override fun onCreate(owner: LifecycleOwner) {
      fragment.viewLifecycleOwnerLiveData.observeForever(observer)
    }

    override fun onDestroy(owner: LifecycleOwner) {
      fragment.viewLifecycleOwnerLiveData.removeObserver(observer)
      fragment.lifecycle.removeObserver(this)

      binding = null
      onDestroyView = null
    }
  }

  companion object Factory {
    /**
     * Create [FragmentViewBindingDelegate] from [viewBindingBind] lambda function.
     *
     * @param fragment the [Fragment] which owns this delegated property.
     * @param viewBindingBind a lambda function that creates a [ViewBinding] instance from [Fragment]'s root view, eg: `T::bind` static method can be used.
     */
    @JvmStatic
    fun <T : ViewBinding> from(
      fragment: Fragment,
      viewBindingBind: (View) -> T,
      onDestroyView: (T.() -> Unit)?
    ): FragmentViewBindingDelegate<T> = FragmentViewBindingDelegate(
      fragment = fragment,
      viewBindingBind = viewBindingBind,
      onDestroyView = onDestroyView
    )

    /**
     * Create [FragmentViewBindingDelegate] from [ViewBinding] class.
     *
     * @param fragment the [Fragment] which owns this delegated property.
     * @param viewBindingClazz Kotlin Reflection will be used to get `T::bind` static method from this class.
     */
    @JvmStatic
    fun <T : ViewBinding> from(
      fragment: Fragment,
      viewBindingClazz: Class<T>,
      onDestroyView: (T.() -> Unit)?
    ): FragmentViewBindingDelegate<T> = FragmentViewBindingDelegate(
      fragment = fragment,
      viewBindingClazz = viewBindingClazz,
      onDestroyView = onDestroyView
    )
  }
}
