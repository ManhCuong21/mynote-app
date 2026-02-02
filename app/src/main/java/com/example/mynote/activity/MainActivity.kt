package com.example.mynote.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.dialog.di.DialogService
import com.example.presentation.dialog.di.DialogType
import com.example.presentation.dialog.list.ListDialogFragment
import com.example.presentation.dialog.text.TextDialogFragment
import com.example.presentation.dialog.permission.PermissionManager
import com.notepad.mynote.privatenote.R
import com.notepad.mynote.privatenote.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseMainActivity() {

    @Inject
    lateinit var dialogNavigationManager: DialogService
    @Inject lateinit var permissionManager: PermissionManager
    private val binding by viewBinding<ActivityMainBinding>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionManager.handleResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CrashlyticsLogger()
        collectDialogEvents()
        permissionManager.bindLauncher(permissionLauncher)
    }

    private fun collectDialogEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dialogNavigationManager.dialogEvent.collect { event ->
                    when (event) {
                        is DialogType.Text -> {
                            TextDialogFragment.getInstance(event.builder)
                                .show(supportFragmentManager, "GlobalText")
                        }

                        is DialogType.List -> {
                            ListDialogFragment.getInstance(event.builder)
                                .show(supportFragmentManager, "GlobalList")
                        }
                    }
                }
            }
        }
    }

    override val navHostFragmentActivityMain: FragmentContainerView
        get() = binding.navHostFragmentActivityMain
}