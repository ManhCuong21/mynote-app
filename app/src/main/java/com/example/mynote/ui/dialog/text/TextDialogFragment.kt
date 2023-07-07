package com.example.mynote.ui.dialog.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.mynote.R
import com.example.mynote.core.navigation.MainNavigator
import com.example.mynote.core.viewbinding.inflateViewBinding
import com.example.mynote.databinding.FragmentTextDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


fun Fragment.showTextDialog(
    tag: String = this::class.java.simpleName,
    init: TextDialogFragment.Builder.() -> Unit,
) {
    val builder = TextDialogFragment.Builder().apply(init)
    TextDialogFragment.getInstance(builder)
        .show(
            requireActivity().supportFragmentManager,
            "${TextDialogFragment.TEXT_DIALOG_FRAGMENT_TAG}.$tag"
        )
}

@AndroidEntryPoint
class TextDialogFragment : DialogFragment() {

    @Inject
    internal lateinit var mainNavigator: MainNavigator

    private var builder: Builder? = null
    private lateinit var binding: FragmentTextDialogBinding

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
                binding.tvContentDialog.let {
                    it.text = textContent
                    it.isVisible = textContent?.isNotEmpty() == true
                }
                binding.btnPositive.let {
                    it.text = positiveButtonText
                    it.isVisible = !positiveButtonText.isNullOrEmpty()
                    it.setOnClickListener {
                        positiveButtonClickListener()
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
                isCancelable = cancelable
            }
        }
    }

    class Builder {
        internal var textTitle: String? = null
            private set
        internal var textContent: String? = null
            private set
        internal var positiveButtonText: String? = null
            private set
        internal var positiveButtonClickListener: () -> Unit = { }
        internal var negativeButtonText: String? = null
            private set
        internal var negativeButtonClickListener: () -> Unit = { }
        internal var cancelable: Boolean = true
            private set

        fun textTitle(title: String) {
            textTitle = title
        }

        fun textContent(content: String) {
            textContent = content
        }

        fun positiveButtonAction(
            text: String,
            listener: () -> Unit,
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
        fun getInstance(builder: Builder): TextDialogFragment {
            return TextDialogFragment().apply { this.builder = builder }
        }

        const val TEXT_DIALOG_FRAGMENT_TAG = "TextDialogFragment"
    }
}