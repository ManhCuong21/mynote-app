package com.example.mynote.ui.addnote

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mynote.R
import com.example.mynote.base.BaseFragment
import com.example.mynote.core.external.AppConstants
import com.example.mynote.core.external.AppConstants.Companion.PATH_IMAGE_NOTE
import com.example.mynote.core.external.FileExtension
import com.example.mynote.core.navigation.MainNavigator
import com.example.mynote.core.viewbinding.viewBinding
import com.example.mynote.databinding.DialogChooseImageAddNoteBinding
import com.example.mynote.databinding.FragmentNoteBinding
import com.example.mynote.ui.addnote.adapter.NoteChooseColorAdapter
import com.example.mynote.ui.addnote.adapter.NoteListImageAdapter
import com.example.mynote.ui.dialog.camera.showCameraDialog
import com.example.mynote.ui.dialog.text.showTextDialog
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
    lateinit var fileExtension: FileExtension

    override val binding: FragmentNoteBinding by viewBinding()
    override val viewModel: NoteViewModel by viewModels()

    private val filePath = PATH_IMAGE_NOTE + SimpleDateFormat(
        AppConstants.FILE_NAME_FORMAT,
        Locale.getDefault()
    ).format(System.currentTimeMillis())


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
    private var pathImage = ""
    private val listImageAdapter by lazy {
        NoteListImageAdapter(onItemClicked = {
            lifecycleScope.launch {
                pathImage = it
                intentSenderLauncherForAndroid10 = intentSenderLauncher
                fileExtension.deleteImageFromFile(
                    fragmentActivity = requireActivity(),
                    intentSenderLauncher = intentSenderLauncher,
                    pathImage = it
                )
                viewModel.dispatch(NoteAction.UpdateListImage)
            }
        })
    }

    private val requestPermissionCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            viewModel.dispatch(NoteAction.CameraPermissionResult(isGranted = result))
        }

    private val requestPermissionStorageLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
//                viewModel.dispatch(NoteAction.StoragePermissionResult(isGranted = result))
            }
        }

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private val intentSenderLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        fileExtension.deleteImageFromFile(
                            fragmentActivity = requireActivity(),
                            intentSenderLauncher = intentSenderLauncherForAndroid10,
                            pathImage = pathImage
                        )
                    }
                }
                viewModel.dispatch(NoteAction.UpdateListImage)
            }
        }
    private lateinit var intentSenderLauncherForAndroid10: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionCameraLauncher.launch(Manifest.permission.CAMERA)
        requestPermissionStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                        val listImage = fileExtension.readImageFromFile(
                            fragmentActivity = requireActivity(),
                            pathChild = filePath
                        )
                        binding.rvImage.isVisible = listImage.isNotEmpty()
                        listImageAdapter.submitList(listImage)
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

        }
    }

    private fun setupRecyclerView() = binding.apply {
        rvChooseColor.apply {
            setHasFixedSize(true)
            adapter = chooseColorAdapter
            chooseColorAdapter.submitList(listColor)
        }
        rvImage.apply {
            adapter = listImageAdapter
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
            val permissionCamera = Manifest.permission.CAMERA
            when (viewModel.stateFlow.value.permissionCameraGranted) {
                true -> showCameraDialog {
                    setFileNameImage(filePath)
                    takePictureAction {
                        viewModel.dispatch(NoteAction.UpdateListImage)
                    }
                }

                false -> checkPermission(permissionCamera)
            }
            dialog.dismiss()
        }
    }

    private fun checkPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.dispatch(NoteAction.CameraPermissionResult(isGranted = true))
            }

            shouldShowRequestPermissionRationale(permission) -> {
                viewModel.dispatch(NoteAction.CameraPermissionResult(isGranted = false))
                requestPermissionCameraLauncher.launch(permission)
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
    }
}