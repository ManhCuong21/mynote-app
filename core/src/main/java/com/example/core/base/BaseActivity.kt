package com.example.core.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity(@LayoutRes layoutRes: Int) : AppCompatActivity(layoutRes) {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupViews()
    bindViewModel()
  }

  // ---------------------------------- SETUP VIEWS ----------------------------------

  @MainThread
  protected abstract fun setupViews()

  // ---------------------------------- BIND VIEW MODEL ----------------------------------

  @MainThread
  protected abstract fun bindViewModel()
}
