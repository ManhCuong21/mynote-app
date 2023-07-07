package com.example.mynote.core.viewbinding

import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

@PublishedApi
internal fun ensureMainThread(): Unit = check(Looper.getMainLooper() == Looper.myLooper()) {
  "Expected to be called on the main thread but was " + Thread.currentThread().name
}

internal object GetBindMethod {
  init {
    ensureMainThread()
  }

  private val methodSignature = View::class.java
  private val methodMap = ArrayMap<Class<out ViewBinding>, Method>()

  internal operator fun <T : ViewBinding> invoke(clazz: Class<T>): Method =
    methodMap.getOrPut(clazz) { clazz.getMethod("bind", methodSignature) }
}

internal object GetInflateMethod {
  init {
    ensureMainThread()
  }

  private val methodMap = ArrayMap<Class<out ViewBinding>, Method>()
  private val threeParamsMethodSignature = arrayOf(
    LayoutInflater::class.java,
    ViewGroup::class.java,
    Boolean::class.java
  )
  private val twoParamsMethodSignature = arrayOf(
    LayoutInflater::class.java,
    ViewGroup::class.java
  )

  internal operator fun <T : ViewBinding> invoke(clazz: Class<T>): Method {
    return methodMap
      .getOrPut(clazz) {
        runCatching { clazz.getMethod("inflate", *threeParamsMethodSignature) }
          .recover { clazz.getMethod("inflate", *twoParamsMethodSignature) }
          .getOrThrow()
      }
  }
}

@PublishedApi
internal fun <T : ViewBinding> getInflateMethod(clazz: Class<T>): Method = GetInflateMethod(clazz)
