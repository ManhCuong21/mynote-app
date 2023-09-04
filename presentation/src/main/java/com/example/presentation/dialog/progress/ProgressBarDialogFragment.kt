package com.example.presentation.dialog.progress

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import com.example.presentation.R
import timber.log.Timber

class ProgressBarDialog(private val onLayoutCallback: (() -> Unit)? = null) : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress_bar_dialog, container, false)
        view?.doOnLayout {
            onLayoutCallback?.invoke()
        }
        return view
    }
}

private val TAG = ProgressBarDialog::class.simpleName

@Suppress("NOTHING_TO_INLINE")
private inline fun Fragment.findProgressBarDialog() =
    childFragmentManager.findFragmentByTag(TAG) as? ProgressBarDialog

@Suppress("NOTHING_TO_INLINE")
private inline fun Fragment.tryDismissProgressBarDialog(isLoading: Boolean) {
    findProgressBarDialog()
        ?.dismissAllowingStateLoss()
        ?.also { Timber.d("$this renderLoadingUI $isLoading successfully - remove existing dialog") }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun DialogFragment.isDialogShowing() = dialog !== null &&
        requireDialog().isShowing &&
        !isRemoving

fun Fragment.renderLoadingUI(isLoading: Boolean, onLayoutCallback: (() -> Unit)? = null) =
    runCatching {
        synchronized(this) {
            if (isLoading) {
                if (findProgressBarDialog()?.isDialogShowing() == true) {
                    return@runCatching
                }
                tryDismissProgressBarDialog(true)
                ProgressBarDialog(onLayoutCallback).show(childFragmentManager, TAG)
            } else {
                tryDismissProgressBarDialog(false)
            }
        }
    }.also {
        val tag = it.fold(onSuccess = { "SUCCESS" }, onFailure = { "FAILURE" })
        Timber.d("[$tag] >>> $this renderLoadingUI $isLoading")
    }