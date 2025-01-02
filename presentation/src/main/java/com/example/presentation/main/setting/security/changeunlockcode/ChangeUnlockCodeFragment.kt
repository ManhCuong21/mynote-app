package com.example.presentation.main.setting.security.changeunlockcode

import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentChangeUnlockCodeBinding

class ChangeUnlockCodeFragment : BaseFragment(R.layout.fragment_change_unlock_code) {
    override val binding: FragmentChangeUnlockCodeBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    override fun setupViews() {
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {

    }

    override fun bindViewModel() {
        // No TODO here
    }
}