package com.example.presentation.dialog.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.presentation.dialog.di.DialogService
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor(
    @param:ActivityContext private val context: Context,
    private val dialogService: DialogService
) : PermissionManager {
    private val activity = context as Activity
    private var pendingAction: (() -> Unit)? = null
    private var requestLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun bindLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        this.requestLauncher = launcher
    }

    override fun handleResult(result: Map<String, Boolean>) {
        if (result.values.all { it }) {
            pendingAction?.invoke()
        }
        pendingAction = null
    }

    override fun requestPermission(
        requests: List<PermissionRequest>,
        onGranted: () -> Unit
    ) {
        val needed = requests.filter {
            Build.VERSION.SDK_INT >= it.minSdk &&
                    ContextCompat.checkSelfPermission(context, it.permission) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isEmpty()) {
            onGranted()
            return
        }

        this.pendingAction = onGranted

        val shouldShowRationale = needed.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it.permission)
        }

        if (shouldShowRationale) {
            requestLauncher?.launch(needed.map { it.permission }.toTypedArray())
        } else {
            showSettingsDialog(needed.first())
        }
    }

    private fun showSettingsDialog(req: PermissionRequest) {
        dialogService.showText {
            textTitle(req.title)
            textContent(req.message)
            positiveButtonAction("Settings") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            }
            negativeButtonAction("Cancel") { pendingAction = null }
        }
    }
}