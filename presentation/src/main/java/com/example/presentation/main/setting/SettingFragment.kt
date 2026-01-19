package com.example.presentation.main.setting

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.DialogTimeFormatBinding
import com.example.presentation.databinding.FragmentSettingBinding
import com.example.presentation.main.setting.rateapp.showRateAppDialog
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment(R.layout.fragment_setting) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentSettingBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    override fun setupViews() {
        setupSwipeButton()
        setupClickListener()
        setupTextView()
    }

    override fun bindViewModel() {
        //NO TODO here
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
        btnCompass.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToCompassFragment)
        }
        btnRateApp.setOnClickListener {
            showRateAppDialog()
        }
        lSecurity.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToSecurityFragment)
        }
        lSyncData.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToSyncFragment)
        }
        btnPrivacy.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.MainFragmentToPrivacyPolicyFragment)
        }
        btnShareApp.setOnClickListener {
            shareApp()
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

    private fun shareApp() {
        val appPackageName = requireActivity().packageName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this app: https://play.google.com/store/apps/details?id=$appPackageName"
            )
        }
        requireActivity().startActivity(Intent.createChooser(shareIntent, "Share App via"))
    }
}