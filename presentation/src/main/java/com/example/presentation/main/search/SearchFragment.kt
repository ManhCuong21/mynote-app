package com.example.presentation.main.search

import android.Manifest
import android.os.Build
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.model.NoteModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.dialog.biometric.BiometricManager
import com.example.presentation.databinding.FragmentSearchBinding
import com.example.presentation.dialog.biometric.ManualAuthDialogManager
import com.example.presentation.dialog.di.DialogService
import com.example.presentation.main.home.listnote.ListNoteAction
import com.example.presentation.main.home.listnote.ListNoteViewModel
import com.example.presentation.main.home.listnote.components.GridSpacingItemDecoration
import com.example.presentation.main.home.listnote.components.NoteActionHandler
import com.example.presentation.main.home.listnote.components.NoteUIHelper
import com.example.presentation.dialog.permission.PermissionManager
import com.example.presentation.dialog.permission.PermissionRequest
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search), NoteActionHandler {

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

    override val binding: FragmentSearchBinding by viewBinding<FragmentSearchBinding>()
    override val viewModel: ListNoteViewModel by viewModels()

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
                        MainNavigator.Direction.MainFragmentToDateTimePickersFragment(
                            note
                        )
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

    override fun setupViews() {
        binding.edtSearchNote.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchKeyword(text.toString())
        }
        setupRecyclerView()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteList.collect { notes ->
                    listNoteAdapter.submitList(notes)
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvNote.apply {
        smoothScrollToPosition(0)
        adapter = listNoteAdapter
        addItemDecoration(GridSpacingItemDecoration(2, 24))
    }
}