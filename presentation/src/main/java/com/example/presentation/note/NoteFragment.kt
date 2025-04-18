package com.example.presentation.note

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.core.base.BaseFragment
import com.example.core.core.external.ActionNote
import com.example.core.core.external.AppConstants
import com.example.core.core.external.formatDate
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.model.ItemChooseColor
import com.example.core.core.model.NoteModel
import com.example.core.core.sharepref.SharedPrefersManager
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.authentication.biometric.showBiometricDialog
import com.example.presentation.databinding.FragmentNoteBinding
import com.example.presentation.dialog.progress.renderLoadingUI
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.adapter.NoteChooseColorAdapter
import com.example.presentation.note.adapter.NoteListRecordAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoteFragment : BaseFragment(R.layout.fragment_note) {
    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override val binding: FragmentNoteBinding by viewBinding()
    override val viewModel: NoteViewModel by activityViewModels()

    private val actionNote by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<NoteFragmentArgs>().value.actionNote }

    private val categoryNote by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<NoteFragmentArgs>().value.category }

    private val noteModel by lazy(LazyThreadSafetyMode.NONE)
    { navArgs<NoteFragmentArgs>().value.noteModel }

    private val listPermission =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    private val listColor = listOf(
        ItemChooseColor(R.color.orangeTitle, R.color.orangeContent),
        ItemChooseColor(R.color.blueTitle, R.color.blueContent),
        ItemChooseColor(R.color.greenTitle, R.color.greenContent),
        ItemChooseColor(R.color.yellowTitle, R.color.yellowContent),
        ItemChooseColor(R.color.violetTitle, R.color.violetContent),
        ItemChooseColor(R.color.redTitle, R.color.redContent),
        ItemChooseColor(R.color.blackTitle, R.color.blackContent),
    )

    private val chooseColorAdapter by lazy {
        NoteChooseColorAdapter(
            defaultPosition = getDefaultPosition(),
            onItemClicked = { position ->
                val colorTitle = resources.getString(listColor[position].colorTitle)
                val colorContent = resources.getString(listColor[position].colorContent)
                setupColorTextInput(colorTitle, colorContent)
                viewModel.dispatch(NoteAction.ColorTitleNoteChanged(colorTitleNote = colorTitle))
                viewModel.dispatch(NoteAction.ColorContentNoteChanged(colorContentNote = colorContent))
            })
    }

    private val listRecordAdapter by lazy {
        NoteListRecordAdapter(onItemDelete = {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.dispatch(NoteAction.DeleteRecordNote(requireActivity(), it))
            }
        })
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dispatch(NoteAction.DeleteDirectoryTemp(requireActivity()))
        requestPermissionLauncher.launch(listPermission)
    }

    override fun setupViews() {
        initialValue()
        if (!viewModel.uiStateFlow.value.isFirstTime) {
            viewModel.dispatch(NoteAction.IsFirstTime)
            setupViewModel()
        } else {
            viewModel.dispatch(NoteAction.GetListRecordNote(requireActivity()))
        }
        setupRecyclerView()
        setupClickListener()
        setupTextInput()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectSingleEvents() }
                launch { collectStateUpdates() }
                launch { collectUiStateUpdates() }
            }
        }
    }

    private fun collectSingleEvents() {
        viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
            when (event) {
                is NoteSingleEvent.SaveFileToTempSuccess -> {
                    viewModel.dispatch(NoteAction.GetListRecordNote(requireActivity()))
                }

                is NoteSingleEvent.GetListRecord -> {
                    binding.rvRecordNote.isVisible = event.list.isNotEmpty()
                    listRecordAdapter.submitList(event.list)
                }

                is NoteSingleEvent.SaveNoteSuccess -> {
                    requireActivity().viewModelStore.clear()
                    mainNavigator.popBackStack()
                }

                is NoteSingleEvent.Failed -> {
                    // Optionally log or show error
                }
            }
        }
    }

    private fun collectStateUpdates() {
        viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
            renderLoadingUI(state.isLoading == true)
        }
    }

    private fun collectUiStateUpdates() {
        viewModel.uiStateFlow.collectIn(viewLifecycleOwner) { state ->
            binding.btnSaveNote.isVisible = !state.titleNote.isNullOrEmpty()
        }
    }

    private fun setupViewModel() {
        if (actionNote == ActionNote.UPDATE_NOTE) {
            noteModel?.let { initItemNote(it) }
        } else {
            categoryNote?.let { viewModel.dispatch(NoteAction.CategoryNoteChanged(it)) }
            viewModel.dispatch(
                NoteAction.DirectoryNameNoteChanged(
                    AppConstants.PATH_MEDIA_NOTE + formatDate(AppConstants.FILE_NAME_FORMAT)
                )
            )
        }
    }

    private fun initItemNote(noteModel: NoteModel) = binding.apply {
        viewModel.dispatch(NoteAction.DirectoryNameNoteChanged(fileMediaNote = noteModel.nameMediaNote))
        viewModel.dispatch(NoteAction.SaveFileMediaToTemp(requireActivity(), noteModel))
        noteModel.categoryNote?.let { viewModel.dispatch(NoteAction.CategoryNoteChanged(it)) }
        edtTitleNote.setText(noteModel.titleNote)
        edtContentNote.setText(noteModel.contentNote)
        val colorTitle =
            noteModel.colorTitleNote.ifEmpty { resources.getString(listColor[0].colorTitle) }
        val colorContent =
            noteModel.colorContentNote.ifEmpty { resources.getString(listColor[0].colorContent) }
        setupColorTextInput(colorTitle, colorContent)
        viewModel.dispatch(NoteAction.TitleNoteChanged(titleNote = noteModel.titleNote))
        viewModel.dispatch(NoteAction.ContentNoteChanged(contentNote = noteModel.contentNote))
        viewModel.dispatch(NoteAction.ColorTitleNoteChanged(colorTitleNote = colorTitle))
        viewModel.dispatch(NoteAction.ColorContentNoteChanged(colorContentNote = colorContent))
        viewModel.dispatch(NoteAction.SecurityNoteChanged(security = noteModel.security))
    }

    private fun setupClickListener() = binding.apply {
        btnBack.setOnClickListener { handleBackClick() }
        btnChooseColor.setOnClickListener { toggleColorPicker() }
        btnChooseImage.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.NoteFragmentToImageNoteFragment)
        }
        btnChooseRecord.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.NoteFragmentToRecorderFragment)
        }
        btnSecurity.setOnClickListener { handleSecurityClick() }
        btnSaveNote.setOnClickListener { handleSaveClick() }
    }

    private fun handleBackClick() {
        viewModel.dispatch(NoteAction.DeleteDirectoryTemp(requireActivity()))
        requireActivity().viewModelStore.clear()
        mainNavigator.popBackStack()
    }

    private fun toggleColorPicker() {
        binding.rvChooseColor.isVisible = !binding.rvChooseColor.isVisible
    }

    private fun handleSecurityClick() {
        val hasPasswordOrBiometric =
            !sharedPrefersManager.passwordNote.isNullOrEmpty() || sharedPrefersManager.isBiometric

        if (hasPasswordOrBiometric) {
            showBiometricDialog {
                textTitle(if (viewModel.uiStateFlow.value.security == true) "Unlock" else "Lock")
                setBiometricSuccessAction {
                    viewModel.uiStateFlow.value.security?.let {
                        viewModel.dispatch(NoteAction.SecurityNoteChanged(!it))
                    }
                }
            }
        } else {
            mainNavigator.navigate(MainNavigator.Direction.NoteFragmentToSecurityFragment)
        }
    }

    private fun handleSaveClick() {
        viewModel.dispatch(NoteAction.DeleteDirectory(requireActivity()))
        viewModel.dispatch(NoteAction.SaveNote(requireActivity(), noteModel, actionNote))
    }

    private fun setupRecyclerView() = binding.apply {
        rvChooseColor.apply {
            setHasFixedSize(true)
            adapter = chooseColorAdapter
            chooseColorAdapter.submitList(listColor)
        }
        rvRecordNote.apply {
            adapter = listRecordAdapter
        }
    }

    private fun initialValue() = binding.apply {
        val state = viewModel.uiStateFlow.value

        edtTitleNote.setText(state.titleNote)
        edtContentNote.setText(state.contentNote)

        val defaultColor = listColor.first()
        val colorTitle = state.colorTitleNote
            ?.takeIf { it.isNotEmpty() }
            ?: resources.getString(defaultColor.colorTitle)

        val colorContent = state.colorContentNote
            ?.takeIf { it.isNotEmpty() }
            ?: resources.getString(defaultColor.colorContent)

        setupColorTextInput(colorTitle, colorContent)

        viewModel.dispatch(NoteAction.ColorTitleNoteChanged(colorTitle))
        viewModel.dispatch(NoteAction.ColorContentNoteChanged(colorContent))
    }

    private fun setupColorTextInput(colorTitle: String, colorContent: String) = binding.apply {
        edtTitleNote.setBackgroundColor(colorTitle.toColorInt())
        edtContentNote.setBackgroundColor(colorContent.toColorInt())
        edtContentNote.colorLine = colorTitle
    }

    private fun setupTextInput() = binding.apply {
        edtTitleNote.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(NoteAction.TitleNoteChanged(text?.toString().orEmpty()))
            }
        }
        edtContentNote.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.dispatch(NoteAction.ContentNoteChanged(text?.toString().orEmpty()))
            }
        }
    }

    private fun getDefaultPosition(): Int {
        return noteModel?.let { model ->
            listColor.indexOfFirst {
                resources.getString(it.colorTitle).equals(model.colorTitleNote, ignoreCase = true)
            }.takeIf { it >= 0 } ?: 0
        } ?: 0
    }
}