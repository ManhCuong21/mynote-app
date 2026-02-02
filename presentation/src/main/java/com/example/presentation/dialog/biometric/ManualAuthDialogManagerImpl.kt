package com.example.presentation.dialog.biometric

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

class ManualAuthDialogManagerImpl @Inject constructor(
    activity: Activity
) : ManualAuthDialogManager {

    private val fragmentActivity = activity as FragmentActivity

    override fun showManualAuth(title: String, onSuccess: () -> Unit) {
        if (fragmentActivity.supportFragmentManager.isStateSaved) return

        // Khởi tạo Builder cho Dialog thủ công
        val builder = ManualAuthDialogFragment.Builder().apply {
            textTitle(title)
            setAuthSuccessAction(onSuccess)
        }

        ManualAuthDialogFragment.getInstance(builder).show(
            fragmentActivity.supportFragmentManager,
            ManualAuthDialogFragment.MANUAL_AUTH_DIALOG_TAG
        )
    }
}