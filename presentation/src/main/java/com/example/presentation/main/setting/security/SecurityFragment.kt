package com.example.presentation.main.setting.security

import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentSecurityBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SecurityFragment : BaseFragment(R.layout.fragment_security) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    override val binding: FragmentSecurityBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    override fun setupViews() {
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {
        btnChangeUnlockCode.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.SecurityFragmentToChangeUnlockCodeFragment)
        }
    }

    override fun bindViewModel() {
        // No TODO here
    }
}
