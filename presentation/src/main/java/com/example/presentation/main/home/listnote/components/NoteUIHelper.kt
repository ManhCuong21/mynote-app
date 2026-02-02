package com.example.presentation.main.home.listnote.components

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.example.core.core.external.ActionNote
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.presentation.R
import com.example.presentation.databinding.DialogSettingNoteBinding
import com.example.presentation.dialog.di.DialogService
import com.example.presentation.main.home.toListDialogItem
import timber.log.Timber
import javax.inject.Inject

class NoteUIHelper @Inject constructor(
    private val fragment: Fragment,
    private val dialogService: DialogService,
    private val layoutInflater: LayoutInflater,
    private val actionHandler: NoteActionHandler,
    private val onRequireBiometric: (() -> Unit) -> Unit,
    private val onRequireOtp: (() -> Unit) -> Unit
) {

    fun createAdapter() = ListNoteAdapter(
        format24Hour = actionHandler.getSettings().first,
        isBiometric = actionHandler.getSettings().second,
        onItemClicked = { showActionDialog(it) },
        onRequireAuth = { note ->
            onRequireBiometric { showActionDialog(note) }
        },
        onRequireOtp = { note ->
            onRequireOtp { showActionDialog(note) }
        }
    )

    fun showActionDialog(noteModel: NoteModel) {
        val activity = fragment.activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }

        val binding = DialogSettingNoteBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.apply {
            val actions = mapOf(
                btnShowOnMap to ActionNote.NOTIFICATION,
                btnEditNote to ActionNote.UPDATE_NOTE,
                btnChangeCategory to ActionNote.CHANGE_CATEGORY,
                btnDeleteNote to ActionNote.DELETE_NOTE
            )

            actions.forEach { (view, action) ->
                view.setOnClickListener {
                    actionHandler.onHandleAction(action, noteModel)
                    dialog.dismiss()
                }
            }
            btnCancel.setOnClickListener { dialog.dismiss() }
        }
        try {
            dialog.show()
        } catch (e: WindowManager.BadTokenException) {
            Timber.e(e, "Cannot display dialog - Activity has died")
        }
    }

    fun showChangeCategoryDialog(
        listCategory: List<CategoryModel>,
        onCategorySelected: (CategoryModel) -> Unit
    ) {
        dialogService.showList {
            textTitle(fragment.getString(R.string.title_dialog_change_category))
            listItem(listCategory.map { it.toListDialogItem() })
            positiveButtonAction(fragment.getString(R.string.title_ok)) { indexItem ->
                onCategorySelected(listCategory[indexItem])
            }
            negativeButtonAction(fragment.getString(R.string.button_cancel)) {}
        }
    }
}