package com.example.presentation.dialog.permission

import android.os.Build

data class PermissionRequest(
    val permission: String,
    val minSdk: Int = Build.VERSION_CODES.BASE,
    val title: String = "Permission Required",
    val message: String = "This feature needs access to work properly. Please allow it."
)