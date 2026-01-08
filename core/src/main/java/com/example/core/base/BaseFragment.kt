package com.example.core.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.PUBLICATION

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    protected open val logTag: String by lazy(PUBLICATION) {
        // Tag length limit was removed in API 26.
        this::class.java.simpleName
    }

    protected abstract val binding: ViewBinding
    protected abstract val viewModel: BaseViewModel

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(logTag).d("onCreate $this")
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(logTag).d("onViewCreated $this")

        setupViews()
        bindViewModel()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            // Hide the bottom navigation view whenever the keyboard is visible.
            // val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            // binding.bottomNavView?.isVisible = !imeVisible

            val insetsStatus = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.updatePadding(
                top = insetsStatus.top
            )
            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        Timber.tag(logTag).d("onStart $this")
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        Timber.tag(logTag).d("onPause $this")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        Timber.tag(logTag).d("onStop $this")
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag(logTag).d("onDestroyView $this")
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(logTag).d("onDestroy $this")
    }

    // ---------------------------------- SETUP VIEWS ----------------------------------

    @MainThread
    protected abstract fun setupViews()

    // ---------------------------------- BIND VIEW MODEL ----------------------------------

    @MainThread
    protected abstract fun bindViewModel()
}
