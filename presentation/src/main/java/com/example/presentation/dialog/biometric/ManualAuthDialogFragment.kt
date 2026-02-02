package com.example.presentation.dialog.biometric

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.core.core.model.AuthMethod
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentBiometricDialogBinding
import com.example.presentation.main.setting.security.manager.OTPUtils
import com.example.presentation.main.setting.security.setupunlockcode.PasswordOTPView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManualAuthDialogFragment : DialogFragment(), PasswordOTPView.OtpCompleteListener {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    private lateinit var binding: FragmentBiometricDialogBinding
    private var builder: Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = layoutInflater.inflateViewBinding(parent = container, attachToParent = false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCommonUi()
        setupAuthMode()
        handleVisibleItem()
    }

    private fun setupCommonUi() = binding.apply {
        builder?.let {
            tvTitleDialog.text = it.textTitle
            tvTitleDialog.isVisible = !it.textTitle.isNullOrEmpty()
        }

        btnNegative.setOnClickListener { dismiss() }
    }

    private fun setupAuthMode() {
        when (sharedPrefersManager.authMethod) {
            AuthMethod.PASSWORD -> setupPasswordMode()
            AuthMethod.PIN -> setupPinMode()
        }
    }

    private fun setupPinMode() = binding.apply {
        edtOtp.setOtpCompleteListener(this@ManualAuthDialogFragment)
        edtOtp.requestFocusAndShowKeyboard()
    }

    private fun setupPasswordMode() = binding.apply {
        // Sử dụng extension core-ktx để rút gọn TextWatcher
        edtPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnPositive.isEnabled = !s.isNullOrEmpty()
            }
        })

        btnPositive.setOnClickListener {
            handleInputComplete(edtPassword.editText?.text.toString())
        }
    }

    private fun handleVisibleItem() = binding.apply {
        val isPassword = sharedPrefersManager.authMethod == AuthMethod.PASSWORD
        val isPin = sharedPrefersManager.authMethod == AuthMethod.PIN

        edtOtp.isVisible = isPin
        edtPassword.isVisible = isPassword
        btnPositive.isVisible = isPassword
    }

    private fun handleInputComplete(input: String) {
        val savedOtp = sharedPrefersManager.passwordNote?.let {
            OTPUtils().decryptOTP(it, "123456789")
        }

        if (input == savedOtp) {
            builder?.setBiometricSuccessListener?.invoke()
            dismiss()
        } else {
            Toast.makeText(context, "OTP does not match, please try again", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    class Builder {
        internal var textTitle: String? = null
        internal var setBiometricSuccessListener: () -> Unit = {}

        fun textTitle(title: String) {
            textTitle = title
        }

        fun setAuthSuccessAction(listener: () -> Unit) {
            setBiometricSuccessListener = listener
        }
    }

    companion object {
        fun getInstance(builder: Builder) = ManualAuthDialogFragment().apply {
            this.builder = builder
        }

        const val MANUAL_AUTH_DIALOG_TAG = "ManualAuthDialogFragment"
    }

    override fun onOtpComplete(otp: String) = handleInputComplete(otp)
}