package com.example.presentation.addnote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.core.base.BaseFragment
import com.example.core.core.external.AppConstants
import com.example.core.core.external.AppConstants.PATH_MEDIA_NOTE
import com.example.core.core.file.image.ImageFile
import com.example.core.core.file.record.RecordFile
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.addnote.adapter.NoteChooseColorAdapter
import com.example.presentation.addnote.adapter.NoteListImageAdapter
import com.example.presentation.addnote.adapter.NoteListRecordAdapter
import com.example.presentation.dialog.camera.showCameraDialog
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.R
import com.example.presentation.databinding.DialogChooseImageAddNoteBinding
import com.example.presentation.databinding.FragmentNoteBinding
import com.example.presentation.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class NoteFragment : BaseFragment(R.layout.fragment_note) {
    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var imageFile: ImageFile

    @Inject
    lateinit var recordFile: RecordFile

    override val binding: FragmentNoteBinding by viewBinding()
    override val viewModel: NoteViewModel by activityViewModels()

    private val listPermission =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val listColor = listOf(
        ItemChooseColor(R.color.orangeTitle, R.color.orangeContent),
        ItemChooseColor(R.color.blueTitle, R.color.blueContent),
        ItemChooseColor(R.color.greenTitle, R.color.greenContent),
        ItemChooseColor(R.color.yellowTitle, R.color.yellowContent),
        ItemChooseColor(R.color.violetTitle, R.color.violetContent),
        ItemChooseColor(R.color.redTitle, R.color.redContent),
        ItemChooseColor(R.color.blackTitle, R.color.blackContent),
    )
    private val pathFile = PATH_MEDIA_NOTE + SimpleDateFormat(
        AppConstants.FILE_NAME_FORMAT,
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    private val chooseColorAdapter by lazy {
        NoteChooseColorAdapter(onItemClicked = { position ->
            val item = listColor[position]
            binding.edtTitleNote.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    item.colorTitle
                )
            )
            binding.edtContentNote.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    item.colorContent
                )
            )
            binding.edtContentNote.colorLine = resources.getString(item.colorTitle)
        })
    }

    private val listImageAdapter by lazy {
        NoteListImageAdapter(onItemDelete = {
            lifecycleScope.launch {
                imageFile.deleteImageFromFile(
                    pathImage = it
                )
                viewModel.dispatch(NoteAction.UpdateListImage)
            }
        })
    }

    private val listRecordAdapter by lazy {
        NoteListRecordAdapter(onItemDelete = {
            lifecycleScope.launch {
                recordFile.deleteRecordFromFile(
                    pathRecord = it
                )
                viewModel.dispatch(NoteAction.UpdateListRecord)
            }
        })
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(listPermission)
    }

    override fun setupViews() {
        setupRecyclerView()
        setupClickListener()
    }

    override fun bindViewModel() {
        lifecycleScope.launch {
            viewModel.singleEventFlow.collect { event ->
                when (event) {
                    is NoteSingleEvent.UpdateListImage -> {
                        val listImage = imageFile.readImageFromFile(
                            fragmentActivity = requireActivity(),
                            pathFile = pathFile
                        )
                        binding.rvImageNote.isVisible = listImage.isNotEmpty()
                        listImageAdapter.submitList(listImage)
                    }

                    is NoteSingleEvent.UpdateListRecord -> {
                        val listRecord = recordFile.readRecordFromFile(
                            fragmentActivity = requireActivity(),
                            pathFile = pathFile
                        )
                        binding.rvRecordNote.isVisible = listRecord.isNotEmpty()
                        listRecordAdapter.submitList(listRecord)
                    }
                }
            }
        }
    }

    private fun setupClickListener() = binding.apply {
        btnBack.setOnClickListener {
            mainNavigator.popBackStack()
        }
        btnChooseColor.setOnClickListener {
            rvChooseColor.isVisible = !rvChooseColor.isVisible
        }
        btnChooseImage.setOnClickListener {
            showDialogChooseImage()
        }
        btnChooseRecord.setOnClickListener {
            mainNavigator.navigate(MainNavigator.Direction.NoteFragmentToRecorderFragment(pathFile))
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

    private fun showDialogChooseImage() = binding.apply {
        val binding = DialogChooseImageAddNoteBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.show()
        binding.btnMyPhoto.setOnClickListener {

        }
        binding.btnCamera.setOnClickListener {
            if (checkPermission()) {
                showCameraDialog {
                    setFileNameImage(pathFile)
                    takePictureAction {
                        viewModel.dispatch(NoteAction.UpdateListImage)
                    }
                }
            }
            dialog.dismiss()
        }
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
}