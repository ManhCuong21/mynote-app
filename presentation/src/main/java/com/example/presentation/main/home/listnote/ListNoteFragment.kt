package com.example.presentation.main.home.listnote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.model.CategoryModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.BiometricAuthenticationManager
import com.example.presentation.databinding.FragmentListNoteBinding
import com.example.presentation.dialog.list.showListDialog
import com.example.presentation.dialog.progress.renderLoadingUI
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.main.home.toListDialogItem
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ListNoteFragment : BaseFragment(R.layout.fragment_list_note) {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var biometricAuthenticationManager: BiometricAuthenticationManager

    override val binding: FragmentListNoteBinding by viewBinding()
    override val viewModel: ListNoteViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private lateinit var categoryNote: CategoryModel
    private val listNoteAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ListNoteAdapter(
            fragment = this,
            format24Hour = sharedPrefersManager.format24Hour,
            isBiometric = sharedPrefersManager.isBiometric,
            biometricAuthenticationManager = biometricAuthenticationManager,
            onItemClicked = { action, noteModel ->
                when (action) {
                    ActionNote.NOTIFICATION -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (checkPermission()) {
                                mainNavigator.navigate(
                                    MainNavigator.Direction.MainFragmentToDateTimePickersFragment(
                                        noteModel
                                    )
                                )
                            }
                        } else {
                            mainNavigator.navigate(
                                MainNavigator.Direction.MainFragmentToDateTimePickersFragment(
                                    noteModel
                                )
                            )
                        }
                    }

                    ActionNote.UPDATE_NOTE -> {
                        mainNavigator.navigate(
                            MainNavigator.Direction.MainFragmentToUpdateNoteFragment(noteModel = noteModel)
                        )
                    }

                    ActionNote.CHANGE_CATEGORY -> {
                        showListDialog {
                            val listCategory = viewModel.stateFlow.value.listCategory
                            textTitle(getString(R.string.title_dialog_change_category))
                            listItem(listCategory.map { it.toListDialogItem() })
                            positionSelected(listCategory.indexOf(noteModel.categoryNote))
                            positiveButtonAction(getString(R.string.title_ok)) { indexItem ->
                                listCategory[indexItem].let {
                                    viewModel.dispatch(
                                        ListNoteAction.ChangeCategoryNote(
                                            noteModel = noteModel,
                                            category = it
                                        )
                                    )
                                }
                            }
                            negativeButtonAction(getString(R.string.button_cancel)) {}
                        }
                    }

                    ActionNote.DELETE_NOTE -> {
                        viewModel.dispatch(ListNoteAction.DeleteNote(requireActivity(), noteModel))
                    }

                    else -> {}
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        viewModel.dispatch(ListNoteAction.GetListNote(categoryNote))
        requestPermissionLauncher.launch(PERMISSION_NOTIFICATION)
    }

    override fun setupViews() {
        setupRecyclerView()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
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
        }
    }

    private fun setupRecyclerView() = binding.rvNote.apply {
        smoothScrollToPosition(0)
        adapter = listNoteAdapter
        addItemDecoration(GridSpacingItemDecoration(2, 24))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(): Boolean {
        when {
            PERMISSION_NOTIFICATION.any {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    PERMISSION_NOTIFICATION
                ) == PackageManager.PERMISSION_GRANTED
            } -> return true

            shouldShowRequestPermissionRationale(PERMISSION_NOTIFICATION) -> {
                requestPermissionLauncher.launch(PERMISSION_NOTIFICATION)
            }

            else -> {
                showTextDialog {
                    textTitle("Permission Denied")
                    textContent("Please grant access in setting")
                    positiveButtonAction("Open") {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${requireActivity().packageName}")
                        appPermissionSettingLauncher.launch(intent)
                    }
                    negativeButtonAction("Cancel") {}
                }
            }
        }
        return false
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val PERMISSION_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS
        fun newInstance(category: CategoryModel) = ListNoteFragment().apply {
            categoryNote = category
        }
    }
}