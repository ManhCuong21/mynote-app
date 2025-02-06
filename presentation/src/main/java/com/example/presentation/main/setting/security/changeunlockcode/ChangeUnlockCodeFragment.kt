package com.example.presentation.main.setting.security.changeunlockcode

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentChangeUnlockCodeBinding
import com.example.presentation.main.setting.security.manager.AuthMethod
import com.example.presentation.main.setting.security.manager.OTPUtils
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeUnlockCodeFragment : BaseFragment(R.layout.fragment_change_unlock_code),
    PasswordOTPView.OtpCompleteListener {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentChangeUnlockCodeBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    private val isSecondAttempt by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<ChangeUnlockCodeFragmentArgs>().value.isSecondAttempt }

    private val firstOtp by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<ChangeUnlockCodeFragmentArgs>().value.firstOtp }

    override fun setupViews() {
        setupOTPView()
        setUpClickListeners()
    }

    private fun setupOTPView() = binding.apply {
        edtOtp.setOtpCompleteListener(this@ChangeUnlockCodeFragment)
        tvTitle.text = if (isSecondAttempt) "Re-enter unlock code" else "Create new unlock code"
    }

    private fun setUpClickListeners() = binding.apply {
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
    }

    override fun onOtpComplete(otp: String) {
        if (isSecondAttempt) {
            if (otp == firstOtp) {
                val encryptedOtp = OTPUtils().encryptOTP(otp, "123456789")
                sharedPrefersManager.otpKey = encryptedOtp
                sharedPrefersManager.authMethod = AuthMethod.PIN.name
                Toast.makeText(context, "Verification successful!", Toast.LENGTH_SHORT).show()
                mainNavigator.popBackStack()
                mainNavigator.popBackStack()
            } else {
                Toast.makeText(context, "OTP does not match, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            mainNavigator.navigate(
                MainNavigator.Direction.ChangeUnlockCodeFragmentToSecondChangeUnlockCodeFragment(
                    isSecondAttempt = true,
                    firstOtp = otp
                )
            )
        }
    }

    override fun bindViewModel() {
        // No TODO here
    }
}