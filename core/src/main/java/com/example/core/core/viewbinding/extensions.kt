package com.example.core.core.viewbinding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

//
// Fragment
//

/**
 * Create [ViewBinding] property delegate for this [Fragment].
 *
 * @param bind a lambda function that creates a [ViewBinding] instance from [Fragment]'s root view, eg: `T::bind` static method can be used.
 */
@MainThread
fun <T : ViewBinding> Fragment.viewBinding(
  bind: (View) -> T,
  onDestroyView: (T.() -> Unit)? = null
): FragmentViewBindingDelegate<T> = FragmentViewBindingDelegate.from(
    fragment = this,
    viewBindingBind = bind,
    onDestroyView = onDestroyView
)

/**
 * Create [ViewBinding] property delegate for this [Fragment].
 */
@MainThread
inline fun <reified T : ViewBinding> Fragment.viewBinding(
  noinline onDestroyView: (T.() -> Unit)? = null
): FragmentViewBindingDelegate<T> = FragmentViewBindingDelegate.from(
    fragment = this,
    viewBindingClazz = T::class.java,
    onDestroyView = onDestroyView
)

@Suppress("unused")
@MainThread
inline fun <reified T : ViewBinding> viewBinding(): ActivityViewBindingDelegate<T> =
  ActivityViewBindingDelegate.from(viewBindingClazz = T::class.java)

/**
 * Inflating a [ViewBinding] of given type [T], This [ViewGroup] is used as a parent.
 *
 * **IMPORTANT!** For inflating views with `merge` at the root, you need to pass `attachToParent` is `true`.
 */
inline infix fun <reified T : ViewBinding> ViewGroup.inflateViewBinding(attachToParent: Boolean): T =
  LayoutInflater.from(context).inflateViewBinding(this, attachToParent)

/**
 * Inflating a [ViewBinding] of given type [T], using the specified [LayoutInflater].
 *
 * **IMPORTANT!** For inflating views with `merge` at the root, you need to pass [attachToParent] as `true`
 * and [parent] must not be `null`.
 */
inline fun <reified T : ViewBinding> LayoutInflater.inflateViewBinding(
  parent: ViewGroup? = null,
  attachToParent: Boolean = parent != null
): T {
  val method = getInflateMethod(T::class.java)
  return if (method.parameterTypes.size == 3) {
    method.invoke(
      null,
      this,
      parent,
      attachToParent
    ) as T
  } else {
    requireNotNull(parent) { "parent must not be null for ${T::class.java.simpleName}.inflate" }
    require(attachToParent) { "attachToParent is always true for ${T::class.java.simpleName}.inflate" }

    method.invoke(
      null,
      this,
      parent
    ) as T
  }
}

/**
 * Inflating a [ViewBinding] of given type [T], using the [LayoutInflater] obtained from this [Context].
 *
 * **IMPORTANT!** For inflating views with `merge` at the root, you need to pass [attachToParent] as `true`
 * and [parent] must not be `null`.
 */
inline fun <reified T : ViewBinding> Context.inflateViewBinding(
  parent: ViewGroup? = null,
  attachToParent: Boolean = parent != null
): T = LayoutInflater.from(this).inflateViewBinding(parent, attachToParent)