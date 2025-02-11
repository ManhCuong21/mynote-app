package com.example.presentation.authentication.biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.BiometricDialogFragment.Companion.BIOMETRIC_DIALOG_FRAGMENT_TAG
import com.example.presentation.databinding.FragmentBiometricDialogBinding
import com.example.presentation.main.setting.security.setupunlockcode.PasswordOTPView
import com.example.presentation.main.setting.security.manager.AuthMethod
import com.example.presentation.main.setting.security.manager.OTPUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

fun Fragment.showBiometricDialog(
    tag: String = this::class.java.simpleName,
    init: BiometricDialogFragment.Builder.() -> Unit,
) {
    val builder = BiometricDialogFragment.Builder().apply(init)
    BiometricDialogFragment.getInstance(builder)
        .show(requireActivity().supportFragmentManager, "$BIOMETRIC_DIALOG_FRAGMENT_TAG.$tag")
}

@AndroidEntryPoint
class BiometricDialogFragment : DialogFragment(), PasswordOTPView.OtpCompleteListener {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

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
        setupDialog()
        handleVisibleItem()
        setupInit()
    }

    private fun setupDialog() = binding.apply {
        when (sharedPrefersManager.authMethod) {
            AuthMethod.PASSWORD.name -> {
                setupPasswordOTP()
            }

            AuthMethod.PIN.name -> {
                setupPinOTP()
            }

            AuthMethod.FINGERPRINT.name -> {

            }
        }
    }

    private fun setupPinOTP() = binding.apply {
        edtOtp.setOtpCompleteListener(this@BiometricDialogFragment)
        builder?.let { builder ->
            builder.run {
                tvTitleDialog.let {
                    it.text = textTitle
                    it.isVisible = textTitle?.isNotEmpty() == true
                }
            }
        }
    }

    private fun setupPasswordOTP() = binding.apply {
        builder?.let { builder ->
            builder.run {
                btnPositive.let {
                    it.isEnabled = !edtPassword.editText?.text.isNullOrEmpty()
                    it.setOnClickListener {
                        handleInputComplete(edtPassword.editText?.text.toString())
                        dismiss()
                    }
                }
            }
        }
    }

    private fun setupInit() = binding.apply {
        builder?.let { builder ->
            builder.run {
                tvTitleDialog.let {
                    it.text = textTitle
                    it.isVisible = textTitle?.isNotEmpty() == true
                }
            }
        }

        btnNegative.let {
            it.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    private fun handleVisibleItem() = binding.apply {
        val visiblePasswordOtp = sharedPrefersManager.authMethod == AuthMethod.PASSWORD.name
        val visiblePinOtp = sharedPrefersManager.authMethod == AuthMethod.PIN.name
//        val visibleFingerprint = sharedPrefersManager.authMethod == AuthMethod.FINGERPRINT.name
        edtOtp.isVisible = visiblePinOtp
        edtPassword.isVisible = visiblePasswordOtp
        btnPositive.isVisible = visiblePasswordOtp
    }

    private fun handleInputComplete(otp: String) {
        val decryptOTP = sharedPrefersManager.otpKey?.let { OTPUtils().decryptOTP(it, "123456789") }
        if (otp == decryptOTP) {
            builder?.setBiometricSuccessListener?.let { it() }
            dismiss()
        } else {
            Toast.makeText(context, "OTP does not match, please try again", Toast.LENGTH_SHORT)
                .show()
        }
    }

    class Builder {
        internal var textTitle: String? = null
            private set
        internal var setBiometricSuccessListener: () -> Unit = { }
            private set

        fun textTitle(title: String) {
            textTitle = title
        }

        fun setBiometricSuccessAction(
            listener: () -> Unit,
        ) {
            setBiometricSuccessListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder): BiometricDialogFragment {
            return BiometricDialogFragment().apply { this.builder = builder }
        }

        const val BIOMETRIC_DIALOG_FRAGMENT_TAG = "BiometricDialogFragment"
    }

    override fun onOtpComplete(otp: String) {
        handleInputComplete(otp)
    }
}