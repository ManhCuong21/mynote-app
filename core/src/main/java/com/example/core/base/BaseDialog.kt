package com.example.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<VB : ViewBinding, B : BaseDialog.BaseBuilder<*>> : DialogFragment() {

    protected lateinit var binding: VB
    protected var builder: B? = null

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateBinding(layoutInflater, container)
        return binding.root
    }

    protected fun setupCommonViews(
        tvTitle: TextView?,
        btnPos: Button?,
        btnNeg: TextView?
    ) {
        builder?.let { b ->
            tvTitle?.apply {
                text = b.textTitle
                isVisible = !b.textTitle.isNullOrEmpty()
            }
            btnPos?.apply {
                text = b.positiveButtonText
                isVisible = !b.positiveButtonText.isNullOrEmpty()
                setOnClickListener {
                    onPositiveClicked()
                    dismiss()
                }
            }
            btnNeg?.apply {
                text = b.negativeButtonText
                isVisible = !b.negativeButtonText.isNullOrEmpty()
                setOnClickListener {
                    b.negativeButtonClickListener()
                    dismiss()
                }
            }
            isCancelable = b.cancelable
        }
    }

    abstract fun onPositiveClicked()

    // Builder chung
    abstract class BaseBuilder<T : BaseBuilder<T>> {
        internal var textTitle: String? = null
        var positiveButtonText: String? = null
        internal var negativeButtonText: String? = null
        internal var negativeButtonClickListener: () -> Unit = {}
        internal var cancelable: Boolean = true

        @Suppress("UNCHECKED_CAST")
        fun textTitle(title: String) = apply { textTitle = title } as T

        @Suppress("UNCHECKED_CAST")
        fun negativeButtonAction(text: String, listener: () -> Unit = {}) = apply {
            negativeButtonText = text
            negativeButtonClickListener = listener
        } as T
    }
}