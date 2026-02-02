package com.example.presentation.dialog.permission

import androidx.activity.result.ActivityResultLauncher

interface PermissionManager {
    fun bindLauncher(launcher: ActivityResultLauncher<Array<String>>)
    fun handleResult(result: Map<String, Boolean>)
    fun requestPermission(requests: List<PermissionRequest>, onGranted: () -> Unit)
}