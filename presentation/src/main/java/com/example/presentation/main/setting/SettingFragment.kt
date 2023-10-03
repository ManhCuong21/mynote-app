package com.example.presentation.main.setting

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.core.core.lifecycle.collectIn
import com.example.presentation.R
import com.example.presentation.databinding.DialogTimeFormatBinding
import com.example.presentation.databinding.FragmentSettingBinding
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.main.setting.rateapp.showRateAppDialog
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment(R.layout.fragment_setting) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentSettingBinding by viewBinding()
    override val viewModel: SettingViewModel by viewModels()

    override fun setupViews() {
        setupInformationView()
        setupSwipeButton()
        setupClickListener()
        setupTextView()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is SettingSingleEvent.SignOutUser.Success -> {
                            sharedPrefersManager.userEmail = null
                            setupInformationView()
                        }

                        is SettingSingleEvent.SignOutUser.Failed -> {
                            showTextDialog {
                                textTitle("Sign out failed")
                                textContent(event.error.message.toString())
                                negativeButtonAction("OK") {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupInformationView() = binding.apply {
        tvAccount.isVisible = sharedPrefersManager.userEmail.isNullOrEmpty()
        btnSignIn.isVisible = sharedPrefersManager.userEmail.isNullOrEmpty()
        lnInformation.isVisible = !sharedPrefersManager.userEmail.isNullOrEmpty()
        btnLogOut.isVisible = !sharedPrefersManager.userEmail.isNullOrEmpty()
        tvEmail.text = sharedPrefersManager.userEmail
    }

    private fun setupTextView() = binding.apply {
        tvTimeFormat.text =
            if (!sharedPrefersManager.format24Hour) getString(R.string.dialog_time_format_12)
            else getString(R.string.dialog_time_format_24)
    }

    private fun setupSwipeButton() = binding.apply {
        switchTheme.apply {
            isChecked = sharedPrefersManager.darkModeTheme
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                sharedPrefersManager.darkModeTheme = isChecked
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        lnInformation.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToUserInformationFragment)
        }
        btnTimeFormat.setOnClickListener {
            showDialogTimeFormat()
        }
        btnSignIn.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToSignInFragment)
        }
        btnLogOut.setOnClickListener {
            viewModel.dispatch(SettingAction.SignOut)
        }
        btnRateApp.setOnClickListener {
            showRateAppDialog()
        }
    }

    private fun showDialogTimeFormat() = binding.apply {
        val binding = DialogTimeFormatBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        val format24Hour = sharedPrefersManager.format24Hour
        binding.apply {
            cbItem12Hour.isChecked = !format24Hour
            cbItem24Hour.isChecked = format24Hour
            lnItem12Hour.setOnClickListener {
                cbItem12Hour.isChecked = true
                cbItem24Hour.isChecked = false
            }
            lnItem24Hour.setOnClickListener {
                cbItem12Hour.isChecked = false
                cbItem24Hour.isChecked = true
            }
            cbItem12Hour.setOnCheckedChangeListener { _, isChecked ->
                cbItem12Hour.isChecked = isChecked
                cbItem24Hour.isChecked = !isChecked
            }
            cbItem24Hour.setOnCheckedChangeListener { _, isChecked ->
                cbItem12Hour.isChecked = !isChecked
                cbItem24Hour.isChecked = isChecked
            }
            btnSave.setOnClickListener {
                val textChoose =
                    if (cbItem12Hour.isChecked) getString(R.string.dialog_time_format_12)
                    else getString(R.string.dialog_time_format_24)
                tvTimeFormat.text = textChoose
                sharedPrefersManager.format24Hour = cbItem24Hour.isChecked
                dialog.dismiss()
            }
        }
    }
}