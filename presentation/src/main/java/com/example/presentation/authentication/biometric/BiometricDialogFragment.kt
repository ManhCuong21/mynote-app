package com.example.presentation.authentication.biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.BiometricDialogFragment.Companion.BIOMETRIC_DIALOG_FRAGMENT_TAG
import com.example.presentation.databinding.FragmentBiometricDialogBinding

fun Fragment.showBiometricDialog(
    tag: String = this::class.java.simpleName,
    init: BiometricDialogFragment.Builder.() -> Unit,
) {
    val builder = BiometricDialogFragment.Builder().apply(init)
    BiometricDialogFragment.getInstance(builder)
        .show(requireActivity().supportFragmentManager, "$BIOMETRIC_DIALOG_FRAGMENT_TAG.$tag")
}

class BiometricDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentBiometricDialogBinding
    private var builder: Builder? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        builder?.let { builder ->
            builder.run {
                binding.apply {
                    tvTitleDialog.let {
                        it.text = textTitle
                        it.isVisible = textTitle?.isNotEmpty() == true
                    }
                    btnPositive.let {
                        it.isEnabled = !edtPassword.editText?.text.isNullOrEmpty()
                        it.setOnClickListener {
                            dismiss()
                        }
                    }
                    btnNegative.let {
                        it.setOnClickListener {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    class Builder {
        internal var textTitle: String? = null
            private set

        fun textTitle(title: String) {
            textTitle = title
        }
    }

    companion object {
        fun getInstance(builder: Builder): BiometricDialogFragment {
            return BiometricDialogFragment().apply { this.builder = builder }
        }

        const val BIOMETRIC_DIALOG_FRAGMENT_TAG = "BiometricDialogFragment"
    }
}