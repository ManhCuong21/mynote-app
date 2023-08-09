package com.example.presentation.dialog.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.core.core.model.ListDialogItem
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListDialogBinding
import com.example.presentation.dialog.list.ListDialogFragment.Companion.LIST_DIALOG_FRAGMENT_TAG

fun Fragment.showListDialog(
    tag: String = this::class.java.simpleName,
    init: ListDialogFragment.Builder.() -> Unit,
) {
    val builder = ListDialogFragment.Builder().apply(init)
    ListDialogFragment.getInstance(builder)
        .show(requireActivity().supportFragmentManager, "$LIST_DIALOG_FRAGMENT_TAG.$tag")
}

class ListDialogFragment : DialogFragment() {

    private var builder: Builder? = null
    private lateinit var binding: FragmentListDialogBinding

    private val dialogAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListDialogAdapter(position = builder?.positionSelected ?: 0,
            onItemClicked = { position ->
                builder?.positionSelected(position)
            })
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = layoutInflater.inflateViewBinding(
            parent = container,
            attachToParent = false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        builder?.let { builder ->
            builder.run {
                binding.tvTitleDialog.let {
                    it.text = textTitle
                    it.isVisible = textTitle?.isNotEmpty() == true
                }
                binding.btnPositive.let {
                    it.text = positiveButtonText
                    it.setOnClickListener {
                        positiveButtonClickListener(positionSelected ?: 0)
                        dismiss()
                    }
                }
                binding.btnNegative.let {
                    it.text = negativeButtonText
                    it.isVisible = !negativeButtonText.isNullOrEmpty()
                    it.setOnClickListener {
                        negativeButtonClickListener()
                        dismiss()
                    }
                }
                binding.rvDialog.let {
                    it.adapter = dialogAdapter
                    dialogAdapter.submitList(list)
                }
                isCancelable = cancelable
            }
        }
    }

    class Builder {
        internal var textTitle: String? = null
            private set
        internal var list: List<ListDialogItem>? = null
            private set
        internal var positionSelected: Int? = null
            private set
        internal var positiveButtonText: String? = null
            private set
        internal var positiveButtonClickListener: (Int) -> Unit = { }
            private set
        internal var negativeButtonText: String? = null
            private set
        internal var negativeButtonClickListener: () -> Unit = { }
        internal var cancelable: Boolean = true
            private set

        fun textTitle(title: String) {
            textTitle = title
        }

        fun listItem(listItem: List<ListDialogItem>) {
            list = listItem
        }

        fun positionSelected(position: Int) {
            positionSelected = position
        }

        fun positiveButtonAction(
            text: String,
            listener: (indexItem: Int) -> Unit,
        ) {
            positiveButtonText = text
            positiveButtonClickListener = listener
        }

        fun negativeButtonAction(
            text: String,
            listener: () -> Unit,
        ) {
            negativeButtonText = text
            negativeButtonClickListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder): ListDialogFragment {
            return ListDialogFragment().apply { this.builder = builder }
        }

        const val LIST_DIALOG_FRAGMENT_TAG = "ListDialogFragment"
    }
}