package com.example.presentation.note.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.viewbinding.viewBinding
import com.example.presentation.R
import com.example.presentation.databinding.DialogChooseImageAddNoteBinding
import com.example.presentation.databinding.FragmentImageNoteBinding
import com.example.presentation.dialog.camera.showCameraDialog
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.adapter.NoteListImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ImageNoteFragment : BaseFragment(R.layout.fragment_image_note) {
    override val binding: FragmentImageNoteBinding by viewBinding()
    override val viewModel: ImageNoteViewModel by viewModels()

    @Inject
    lateinit var mainNavigator: MainNavigator

    private val listPermission =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

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

    private val listImageAdapter by lazy {
        NoteListImageAdapter(onItemDelete = {
            viewModel.dispatch(ImageNoteAction.DeleteImageNote(requireActivity(), it))
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dispatch(ImageNoteAction.GetListImageNote(requireActivity()))
    }

    override fun setupViews() {
        binding.apply {
            rvImageNote.apply {
                adapter = listImageAdapter
            }
            btnBack.setOnClickListener {
                mainNavigator.popBackStack()
            }
            btnAddImage.setOnClickListener {
                showDialogChooseImage()
            }
        }
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { event ->
                    when (event) {
                        is ImageNoteSingleEvent.GetListImage -> {
                            binding.rvImageNote.isVisible = event.list.isNotEmpty()
                            listImageAdapter.submitList(event.list)
                        }
                    }
                }
            }
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
        viewModel.dispatch(ImageNoteAction.SaveImageNote(requireActivity(), bitmap))
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
                        viewModel.dispatch(ImageNoteAction.GetListImageNote(requireActivity()))
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

            shouldShowRequestPermissionRationale(listPermission[2]) -> {
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