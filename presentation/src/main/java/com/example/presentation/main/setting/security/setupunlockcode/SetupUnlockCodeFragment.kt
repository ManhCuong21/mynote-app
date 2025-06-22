package com.example.presentation.main.setting.security.setupunlockcode

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.AuthMethod
import com.example.presentation.databinding.FragmentChangeUnlockCodeBinding
import com.example.presentation.main.setting.security.manager.OTPUtils
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupUnlockCodeFragment : BaseFragment(R.layout.fragment_change_unlock_code),
    PasswordOTPView.OtpCompleteListener {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentChangeUnlockCodeBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    private val authMethod by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<SetupUnlockCodeFragmentArgs>().value.authMethod }

    private val isAfterConfirm by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<SetupUnlockCodeFragmentArgs>().value.isAfterConfirm }

    private val isSecondInput by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<SetupUnlockCodeFragmentArgs>().value.isSecondInput }

    private val firstOtp by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<SetupUnlockCodeFragmentArgs>().value.firstOtp }

    private var isHasPassword: Boolean = false
    private var isOtherMethod: Boolean = false

    override fun setupViews() {
        isHasPassword = !sharedPrefersManager.passwordNote.isNullOrEmpty()
        isOtherMethod = authMethod.name != sharedPrefersManager.authMethod
        setupVisibleView()
        setUpClickListeners()
    }

    private fun setupVisibleView() = binding.apply {
        tvTitle.text =
            if (isSecondInput) "Re-enter unlock code" else if (isAfterConfirm || isOtherMethod) "Create new unlock code" else "Enter current unlock code"
        val isOTPView = authMethod == AuthMethod.PIN
        edtOtp.isVisible = isOTPView
        edtPassword.isVisible = !isOTPView
        btnPositive.isVisible = !isOTPView
        if (isOTPView) setupOTPView() else setupPasswordView()
    }

    private fun setupOTPView() = binding.apply {
        edtOtp.setOtpCompleteListener(this@SetupUnlockCodeFragment)
        edtOtp.setTextColorFromAttr(R.attr.customTextColor)
    }

    private fun setupPasswordView() = binding.apply {
        btnPositive.setOnClickListener {
            val password = edtPassword.editText?.text.toString()
            if (password.isNotEmpty()) {
                handleInputComplete(password)
            } else {
                Toast.makeText(context, "Password cannot be blank", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setUpClickListeners() = binding.apply {
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }

    private fun handleInputComplete(otp: String) {
        if (isSecondInput) {
            if (otp == firstOtp) {
                val encryptedOtp = OTPUtils().encryptOTP(otp, "123456789")
                sharedPrefersManager.passwordNote = encryptedOtp
                sharedPrefersManager.authMethod = authMethod.name
                Toast.makeText(context, "Verification successful!", Toast.LENGTH_SHORT)
                    .show()
                if (isHasPassword) {
                    mainNavigator.popBackStack()
                }
                mainNavigator.popBackStack()
                mainNavigator.popBackStack()
            } else {
                Toast.makeText(context, "OTP does not match, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (isAfterConfirm || isOtherMethod) {
            mainNavigator.navigate(
                MainNavigator.Direction.SetupUnlockCodeFragmentToSecondSetupUnlockCodeFragment(
                    authMethod = authMethod,
                    isAfterConfirm = false,
                    isSecondInput = true,
                    firstOtp = otp
                )
            )
        } else {
            val decryptOTP = OTPUtils().decryptOTP(sharedPrefersManager.passwordNote!!, "123456789")
            if (otp == decryptOTP) {
                mainNavigator.navigate(
                    MainNavigator.Direction.SetupUnlockCodeFragmentToSecondSetupUnlockCodeFragment(
                        authMethod = authMethod,
                        isAfterConfirm = true,
                        isSecondInput = false,
                        firstOtp = null
                    )
                )
            } else {
                Toast.makeText(context, "OTP does not match, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onOtpComplete(otp: String) {
        handleInputComplete(otp)
    }

    override fun bindViewModel() {
        // No TODO here
    }
}