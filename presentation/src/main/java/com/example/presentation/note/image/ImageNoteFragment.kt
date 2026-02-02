package com.example.presentation.note.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ExperimentalGetImage
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
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
import com.example.presentation.dialog.permission.PermissionManager
import com.example.presentation.dialog.permission.PermissionRequest
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.NoteFragment.Companion.IMAGE_HAS
import com.example.presentation.note.NoteFragment.Companion.IMAGE_RESULT
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

    @Inject
    lateinit var permissionManager: PermissionManager

    private val listPermission =
        listOf(
            PermissionRequest(permission = Manifest.permission.CAMERA),
            PermissionRequest(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PermissionRequest(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
        )
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
        NoteListImageAdapter(
            onItemEdit = {
                mainNavigator.navigate(
                    MainNavigator.Direction.ImageNoteFragmentToEditImageNoteFragment(it)
                )
            },
            onItemDelete = {
                viewModel.dispatch(ImageNoteAction.DeleteImageNote(it))
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.dispatch(ImageNoteAction.GetListImageNote)
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
                            binding.apply {
                                progress.visibility = View.GONE
                                lnImageNote.visibility = View.VISIBLE
                                rvImageNote.isVisible = event.list.isNotEmpty()
                            }
                            listImageAdapter.submitList(event.list)
                            setFragmentResult(
                                IMAGE_RESULT,
                                bundleOf(IMAGE_HAS to event.list.isNotEmpty())
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveImage(imageUri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(
                    requireContext().applicationContext.contentResolver,
                    imageUri
                )
            } else {
                val source = ImageDecoder.createSource(
                    requireContext().applicationContext.contentResolver,
                    imageUri
                )
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.setOnPartialImageListener {
                        // Nếu ảnh lỗi một phần, vẫn cố gắng load phần còn lại
                        true
                    }
                }
            }

            if (bitmap != null) {
                viewModel.dispatch(ImageNoteAction.SaveImageNote(bitmap))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun showDialogChooseImage() = binding.apply {
        val binding = DialogChooseImageAddNoteBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        binding.btnMyPhoto.setOnClickListener {
            permissionManager.requestPermission(listPermission) {
                val intent = Intent()
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                resultLauncher.launch(intent)
                dialog.dismiss()
            }
        }
        binding.btnCamera.setOnClickListener {
            permissionManager.requestPermission(listPermission) {
                showCameraDialog {
                    takePictureAction {
                        viewModel.dispatch(ImageNoteAction.GetListImageNote)
                    }
                }
            }
            dialog.dismiss()
        }
    }
}