package com.example.presentation.main.setting.security.manager

import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.AuthMethod
import com.example.presentation.authentication.biometric.BiometricAuthenticationManager
import com.example.presentation.databinding.FragmentSecurityBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SecurityFragment : BaseFragment(R.layout.fragment_security) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var biometricAuthenticationManager: BiometricAuthenticationManager

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentSecurityBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")
    private var isSwitchListenerEnabled = true

    override fun setupViews() {
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
        btnSetupPassword.setOnClickListener {
            mainNavigator.navigate(
                MainNavigator.Direction.SecurityFragmentToSetupUnlockCodeFragment(
                    AuthMethod.PASSWORD
                )
            )
        }

        btnSetupPin.setOnClickListener {
            mainNavigator.navigate(
                MainNavigator.Direction.SecurityFragmentToSetupUnlockCodeFragment(
                    AuthMethod.PIN
                )
            )
        }

        switchAuthentication.isChecked = sharedPrefersManager.isBiometric
        switchAuthentication.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchListenerEnabled) return@setOnCheckedChangeListener

            biometricAuthenticationManager.verifyBiometric(
                requireActivity(),
                onSucceeded = {
                    sharedPrefersManager.isBiometric = isChecked
                },
                onFailed = {
                    isSwitchListenerEnabled = false
                    switchAuthentication.isChecked = !isChecked
                    isSwitchListenerEnabled = true
                }
            )
        }
    }

    override fun bindViewModel() {
        // No TODO here
    }
}
