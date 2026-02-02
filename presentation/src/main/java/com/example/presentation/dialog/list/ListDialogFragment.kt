package com.example.presentation.dialog.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.core.base.BaseDialog
import com.example.core.core.model.ListDialogItem
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListDialogBinding

class ListDialogFragment : BaseDialog<FragmentListDialogBinding, ListDialogFragment.Builder>() {

    private val dialogAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListDialogAdapter(position = builder?.positionSelected ?: 0) { position ->
            builder?.positionSelected = position
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        inflater.inflateViewBinding<FragmentListDialogBinding>(container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommonViews(binding.tvTitleDialog, binding.btnPositive, binding.btnNegative)

        binding.rvDialog.apply {
            adapter = dialogAdapter
            dialogAdapter.submitList(builder?.list)
        }
    }

    override fun onPositiveClicked() {
        builder?.positiveButtonClickListener?.invoke(builder?.positionSelected ?: 0)
    }

    class Builder : BaseBuilder<Builder>() {
        internal var list: List<ListDialogItem>? = null
        internal var positionSelected: Int = 0
        internal var positiveButtonClickListener: ((Int) -> Unit)? = null

        fun listItem(items: List<ListDialogItem>) = apply { list = items }
        fun positiveButtonAction(text: String, listener: (Int) -> Unit) = apply {
            positiveButtonText = text
            positiveButtonClickListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder) = ListDialogFragment().apply { this.builder = builder }
    }
}