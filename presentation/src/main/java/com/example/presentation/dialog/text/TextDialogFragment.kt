package com.example.presentation.dialog.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.core.base.BaseDialog
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentTextDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TextDialogFragment : BaseDialog<FragmentTextDialogBinding, TextDialogFragment.Builder>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        inflater.inflateViewBinding<FragmentTextDialogBinding>(container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommonViews(binding.tvTitleDialog, binding.btnPositive, binding.btnNegative)

        binding.tvContentDialog.apply {
            text = builder?.textContent
            isVisible = !builder?.textContent.isNullOrEmpty()
        }
    }

    override fun onPositiveClicked() {
        builder?.positiveButtonClickListener?.invoke()
    }

    class Builder : BaseBuilder<Builder>() {
        internal var textContent: String? = null
        internal var positiveButtonClickListener: (() -> Unit)? = null

        fun textContent(content: String) = apply { textContent = content }
        fun positiveButtonAction(text: String, listener: () -> Unit) = apply {
            positiveButtonText = text
            positiveButtonClickListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder) = TextDialogFragment().apply { this.builder = builder }
    }
}