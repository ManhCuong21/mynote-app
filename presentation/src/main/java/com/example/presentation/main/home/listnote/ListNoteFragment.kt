package com.example.presentation.main.home.listnote

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.lifecycle.collectInViewLifecycle
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentListNoteBinding
import com.example.presentation.dialog.biometric.BiometricManager
import com.example.presentation.dialog.biometric.ManualAuthDialogManager
import com.example.presentation.dialog.di.DialogService
import com.example.presentation.dialog.progress.renderLoadingUI
import com.example.presentation.main.home.listnote.components.GridSpacingItemDecoration
import com.example.presentation.main.home.listnote.components.NoteActionHandler
import com.example.presentation.main.home.listnote.components.NoteUIHelper
import com.example.presentation.dialog.permission.PermissionManager
import com.example.presentation.dialog.permission.PermissionRequest
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ListNoteFragment : BaseFragment(R.layout.fragment_list_note), NoteActionHandler {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var biometricManager: BiometricManager

    @Inject
    lateinit var dialogService: DialogService

    @Inject
    lateinit var manualAuthDialogManager: ManualAuthDialogManager

    @Inject
    lateinit var permissionManager: PermissionManager

    override val binding: FragmentListNoteBinding by viewBinding {
        rvNote.adapter = null
    }

    override val viewModel: ListNoteViewModel by viewModels()
    private lateinit var categoryNote: CategoryModel

    private val noteUIHelper by lazy {
        NoteUIHelper(
            fragment = this,
            dialogService = dialogService,
            layoutInflater = layoutInflater,
            actionHandler = this,
            onRequireBiometric = { onSuccess ->
                biometricManager.verifyBiometric(
                    onSucceeded = onSuccess,
                    onFailed = {})
            },
            onRequireOtp = { onSuccess ->
                manualAuthDialogManager.showManualAuth("Enter otp code") { onSuccess() }
            }
        )
    }

    private val listNoteAdapter by lazy(LazyThreadSafetyMode.NONE) {
        noteUIHelper.createAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
    }

    override fun setupViews() {
        val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(KEY_CATEGORY, CategoryModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(KEY_CATEGORY)
        }

        if (category != null) {
            categoryNote = category
        }
        setupRecyclerView()
    }

    override fun bindViewModel() {
        viewModel.singleEventFlow.collectInViewLifecycle(this) { event ->
            when (event) {
                is ListNoteSingleEvent.GetListNoteSuccess -> {
                    listNoteAdapter.submitList(event.listNote)
                    binding.rvNote.isVisible = event.listNote.isNotEmpty()
                    binding.lnEmptyNote.isVisible = event.listNote.isEmpty()
                }

                is ListNoteSingleEvent.UpdateNote -> {
                    viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
                }

                is ListNoteSingleEvent.DeleteNoteSuccess -> {
                    viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
                }

                is ListNoteSingleEvent.SingleEventFailed -> {
                    Timber.e(event.error)
                }
            }
        }
        viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
            renderLoadingUI(state.isLoading == true)
        }
    }

    private fun setupRecyclerView() = binding.rvNote.apply {
        smoothScrollToPosition(0)
        adapter = listNoteAdapter
        addItemDecoration(GridSpacingItemDecoration(2, 24))
    }

    override fun getSettings(): Pair<Boolean, Boolean> =
        sharedPrefersManager.format24Hour to sharedPrefersManager.isBiometric

    override fun onHandleAction(
        action: ActionNote,
        note: NoteModel
    ) {
        when (action) {
            ActionNote.NOTIFICATION -> {
                val listPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(
                        PermissionRequest(
                            permission = Manifest.permission.POST_NOTIFICATIONS,
                            minSdk = Build.VERSION_CODES.TIRAMISU,
                            title = "Permission Required",
                            message = "Please grant access in settings to receive notifications"
                        )
                    )
                } else {
                    emptyList()
                }
                permissionManager.requestPermission(requests = listPermission) {
                    mainNavigator.navigate(
                        MainNavigator.Direction.MainFragmentToDateTimePickersFragment(note)
                    )
                }
            }

            ActionNote.UPDATE_NOTE -> mainNavigator.navigate(
                MainNavigator.Direction.MainFragmentToUpdateNoteFragment(note)
            )

            ActionNote.CHANGE_CATEGORY -> {
                noteUIHelper.showChangeCategoryDialog(
                    listCategory = viewModel.stateFlow.value.listCategory,
                    onCategorySelected = { newCategory ->
                        viewModel.dispatch(ListNoteAction.ChangeCategoryNote(note, newCategory))
                    }
                )
            }

            ActionNote.DELETE_NOTE -> viewModel.dispatch(ListNoteAction.DeleteNote(note))
            else -> Unit
        }
    }

    companion object {
        private const val KEY_CATEGORY = "key_category"

        fun newInstance(category: CategoryModel): ListNoteFragment {
            val fragment = ListNoteFragment()
            val bundle = Bundle().apply {
                putParcelable(KEY_CATEGORY, category)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}