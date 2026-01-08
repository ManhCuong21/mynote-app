package com.example.presentation.main.search

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.biometric.BiometricAuthenticationManager
import com.example.presentation.databinding.FragmentSearchBinding
import com.example.presentation.dialog.list.showListDialog
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.main.home.listnote.GridSpacingItemDecoration
import com.example.presentation.main.home.listnote.ListNoteAction
import com.example.presentation.main.home.listnote.ListNoteAdapter
import com.example.presentation.main.home.listnote.ListNoteFragment.Companion.PERMISSION_NOTIFICATION
import com.example.presentation.main.home.listnote.ListNoteViewModel
import com.example.presentation.main.home.toListDialogItem
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search) {

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var biometricAuthenticationManager: BiometricAuthenticationManager

    override val binding: FragmentSearchBinding by viewBinding<FragmentSearchBinding>()
    override val viewModel: ListNoteViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

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
                        viewModel.dispatch(ListNoteAction.DeleteNote(noteModel))
                    }

                    else -> {}
                }
            })
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
                        intent.data = "package:${requireActivity().packageName}".toUri()
                        appPermissionSettingLauncher.launch(intent)
                    }
                    negativeButtonAction("Cancel") {}
                }
            }
        }
        return false
    }
}