package com.example.presentation.main.setting

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import com.example.core.base.BaseFragment
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.DialogTimeFormatBinding
import com.example.presentation.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment(R.layout.fragment_setting) {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentSettingBinding by viewBinding()
    override val viewModel: SettingViewModel by viewModels()

    override fun setupViews() {
        setupSwipeButton()
        setupClickListener()
        setupTextView()
    }

    override fun bindViewModel() {
        //No TODO here
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
        btnTimeFormat.setOnClickListener {
            showDialogTimeFormat()
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