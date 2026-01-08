package com.example.core.core.viewbinding

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityViewBindingDelegate<T : ViewBinding> private constructor(
  viewBindingBind: ((View) -> T)? = null,
  viewBindingClazz: Class<T>? = null
) : ReadOnlyProperty<Activity, T> {

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
  }

  override fun getValue(thisRef: Activity, property: KProperty<*>): T {
    return binding
      ?: bind(
        checkNotNull(thisRef.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)) {
          """Content view of Activity $thisRef must be not null before access ViewBinding property. This can be done easily with constructor:
            |
            |public androidx.appcompat.app.AppCompatActivity(@LayoutRes int contentLayoutId) { ... }
            |
            |eg.
            |
            |class MainActivity : AppCompatActivity(R.layout.activity_main) { ... }
            |
          """.trimMargin()
        }
      ).also { binding = it }
  }

  companion object Factory {
    /**
     * Create [ActivityViewBindingDelegate] from [viewBindingBind] lambda function.
     *
     * @param viewBindingBind a lambda function that creates a [ViewBinding] instance from [Activity]'s contentView, eg: `T::bind` static method can be used.
     */
    @JvmStatic
    fun <T : ViewBinding> from(viewBindingBind: (View) -> T): ActivityViewBindingDelegate<T> =
      ActivityViewBindingDelegate(viewBindingBind = viewBindingBind)

    /**
     * Create [ActivityViewBindingDelegate] from [ViewBinding] class.
     *
     * @param viewBindingClazz Kotlin Reflection will be used to get `T::bind` static method from this class.
     */
    @JvmStatic
    fun <T : ViewBinding> from(viewBindingClazz: Class<T>): ActivityViewBindingDelegate<T> =
      ActivityViewBindingDelegate(viewBindingClazz = viewBindingClazz)
  }
}
