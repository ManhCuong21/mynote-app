package com.example.presentation.note

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.DialogChooseImageAddNoteBinding
import com.example.presentation.databinding.FragmentNoteBinding
import com.example.presentation.dialog.camera.showCameraDialog
import com.example.presentation.dialog.progress.renderLoadingUI
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.adapter.NoteChooseColorAdapter
import com.example.presentation.note.adapter.NoteListImageAdapter
import com.example.presentation.note.adapter.NoteListRecordAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoteFragment : BaseFragment(R.layout.fragment_note) {
    @Inject
    lateinit var mainNavigator: MainNavigator

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

    private val listImageAdapter by lazy {
        NoteListImageAdapter(onItemDelete = {
            viewModel.dispatch(NoteAction.DeleteImageNote(requireActivity(), it))
        })
    }

    private val listRecordAdapter by lazy {
        NoteListRecordAdapter(onItemDelete = {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.dispatch(
                    NoteAction.DeleteRecordNote(requireActivity(), it)
                )
            }
        })
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val selectedPhotoUri = intent?.data
                if (intent?.clipData != null) {
                    for (i in 0 until intent.clipData!!.itemCount) {
                        saveImage(intent.clipData!!.getItemAt(i).uri)
                    }
                } else if (intent?.data != null) {
                    selectedPhotoUri?.let { saveImage(it) }
                }
            }
        }

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dispatch(NoteAction.DeleteDirectoryTemp(requireActivity()))
        requestPermissionLauncher.launch(listPermission)
    }

    override fun setupViews() {
        initialValue()
        setupViewModel()
        setupRecyclerView()
        setupClickListener()
        setupTextInput()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is NoteSingleEvent.SaveFileToTempSuccess -> {
                            viewModel.dispatch(NoteAction.GetListImageNote(requireActivity()))
                            viewModel.dispatch(NoteAction.GetListRecordNote(requireActivity()))
                        }

                        is NoteSingleEvent.GetListImage -> {
                            binding.rvImageNote.isVisible = event.list.isNotEmpty()
                            listImageAdapter.submitList(event.list)
                        }

                        is NoteSingleEvent.GetListRecord -> {
                            binding.rvRecordNote.isVisible = event.list.isNotEmpty()
                            listRecordAdapter.submitList(event.list)
                        }

                        is NoteSingleEvent.SaveNoteSuccess -> {
                            requireActivity().viewModelStore.clear()
                            mainNavigator.popBackStack()
                        }

                        is NoteSingleEvent.Failed -> {}
                    }
                }
                viewModel.stateFlow.collectIn(viewLifecycleOwner) { state ->
                    renderLoadingUI(state.isLoading == true)
                }
                viewModel.uiStateFlow.collectIn(viewLifecycleOwner) { state ->
                    binding.btnSaveNote.isVisible = !state.titleNote.isNullOrEmpty()
                }
            }
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
    }

    private fun setupClickListener() = binding.apply {
        btnBack.setOnClickListener {
            viewModel.dispatch(NoteAction.DeleteDirectoryTemp(requireActivity()))
            requireActivity().viewModelStore.clear()
            mainNavigator.popBackStack()
        }
        btnChooseColor.setOnClickListener {
            rvChooseColor.isVisible = !rvChooseColor.isVisible
        }
        btnChooseImage.setOnClickListener {
            showDialogChooseImage()
        }
        btnChooseRecord.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.NoteFragmentToRecorderFragment)
        }
        btnSaveNote.setOnClickListener {
            viewModel.dispatch(NoteAction.DeleteDirectory(requireActivity()))
            viewModel.dispatch(NoteAction.SaveNote(requireActivity(), noteModel, actionNote))
        }
    }

    private fun setupRecyclerView() = binding.apply {
        rvChooseColor.apply {
            setHasFixedSize(true)
            adapter = chooseColorAdapter
            chooseColorAdapter.submitList(listColor)
        }
        rvImageNote.apply {
            adapter = listImageAdapter
        }
        rvRecordNote.apply {
            adapter = listRecordAdapter
        }
    }

    private fun initialValue() = binding.apply {
        val state = viewModel.uiStateFlow.value
        edtTitleNote.setText(state.titleNote)
        edtContentNote.setText(state.contentNote)
        val colorTitle =
            if (!state.colorTitleNote.isNullOrEmpty()) state.colorTitleNote else resources.getString(
                listColor[0].colorTitle
            )
        val colorContent =
            if (!state.colorContentNote.isNullOrEmpty()) state.colorContentNote else resources.getString(
                listColor[0].colorContent
            )
        setupColorTextInput(colorTitle, colorContent)
        viewModel.dispatch(NoteAction.ColorTitleNoteChanged(colorTitleNote = colorTitle))
        viewModel.dispatch(NoteAction.ColorContentNoteChanged(colorContentNote = colorContent))
    }

    private fun setupColorTextInput(colorTitle: String, colorContent: String) = binding.apply {
        edtTitleNote.setBackgroundColor(Color.parseColor(colorTitle))
        edtContentNote.setBackgroundColor(Color.parseColor(colorContent))
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

    private fun showDialogChooseImage() = binding.apply {
        val binding = DialogChooseImageAddNoteBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        binding.btnMyPhoto.setOnClickListener {
            if (checkPermission()) {
                val intent = Intent()
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                resultLauncher.launch(intent)
                dialog.dismiss()
            }
        }
        binding.btnCamera.setOnClickListener {
            if (checkPermission()) {
                showCameraDialog {
                    takePictureAction {
                        viewModel.dispatch(NoteAction.GetListImageNote(requireActivity()))
                    }
                }
            }
            dialog.dismiss()
        }
    }

    private fun saveImage(imageUri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                imageUri
            )
        } else {
            val source = ImageDecoder.createSource(
                requireActivity().contentResolver,
                imageUri
            )
            ImageDecoder.decodeBitmap(source)
        }
        viewModel.dispatch(NoteAction.SaveImageNote(requireActivity(), bitmap))
    }

    private fun checkPermission(): Boolean {
        when {
            listPermission.any {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            } -> return true

            shouldShowRequestPermissionRationale(listPermission[0]) -> {
                requestPermissionLauncher.launch(listPermission)
            }

            shouldShowRequestPermissionRationale(listPermission[1]) -> {
                requestPermissionLauncher.launch(listPermission)
            }

            shouldShowRequestPermissionRationale(listPermission[2]) -> {
                requestPermissionLauncher.launch(listPermission)
            }

            shouldShowRequestPermissionRationale(listPermission[3]) -> {
                requestPermissionLauncher.launch(listPermission)
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

    private fun getDefaultPosition(): Int {
        var position = 0
        if (noteModel != null) {
            listColor.forEachIndexed { index, itemChooseColor ->
                run {
                    if (resources.getString(itemChooseColor.colorTitle)
                            .equals(noteModel?.colorTitleNote, true)
                    ) {
                        position = index
                    }
                }
            }
        }
        return position
    }
}