package com.example.presentation.note.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.base.BaseFragment
import com.example.core.core.external.AppConstants.FILE_NAME_FORMAT
import com.example.core.core.external.formatDate
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.lifecycle.collectIn
import com.example.core.core.model.StatusRecord
import com.example.core.core.viewbinding.viewBinding
import com.example.domain.usecase.file.FileUseCase
import com.example.presentation.R
import com.example.presentation.databinding.FragmentRecorderBinding
import com.example.presentation.dialog.text.showTextDialog
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.NoteAction
import com.example.presentation.note.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class RecorderFragment : BaseFragment(R.layout.fragment_recorder) {

    @Inject
    lateinit var mainNavigator: MainNavigator

    @Inject
    lateinit var fileUseCase: FileUseCase

    override val binding: FragmentRecorderBinding by viewBinding()
    override val viewModel: RecorderViewModel by viewModels()
    private val noteViewModel: NoteViewModel by activityViewModels()

    private var recorder: MediaRecorder? = null

    private val listPermission =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    private val appPermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(listPermission)
    }

    override fun setupViews() {
        setupClickListener()
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.singleEventFlow.collectIn(viewLifecycleOwner) { time ->
                    binding.tvTimerRecord.text = time
                }
            }
        }
    }

    private var lastClickTime: Long = 0
    private fun setupClickListener() = binding.apply {
        var statusRecord = StatusRecord.CREATE
        btnRecording.setOnClickListener {
            if (abs(SystemClock.elapsedRealtime() - lastClickTime) > 1000) {
                if (checkPermission()) {
                    statusRecord = when (statusRecord) {
                        StatusRecord.CREATE -> StatusRecord.START
                        StatusRecord.START -> StatusRecord.PAUSE
                        StatusRecord.PAUSE -> StatusRecord.RESUME
                        StatusRecord.RESUME -> StatusRecord.PAUSE
                    }
                    onRecord(statusRecord)
                }
                lastClickTime = SystemClock.elapsedRealtime()
            }
        }
        btnSaveRecord.setOnClickListener {
            stopRecording()
            noteViewModel.dispatch(NoteAction.GetListImageNote(requireActivity()))
            noteViewModel.dispatch(NoteAction.GetListRecordNote(requireActivity()))
            mainNavigator.popBackStack()
        }
        btnCancel.setOnClickListener {
            stopRecording()
            mainNavigator.popBackStack()
        }
        btnBack.setOnClickListener {
            stopRecording()
            mainNavigator.popBackStack()
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireActivity())
        } else {
            MediaRecorder()
        }
    }

    private fun onRecord(status: StatusRecord) {
        setUiRecording(status)
        when (status) {
            StatusRecord.START -> startRecording()
            StatusRecord.PAUSE -> pauseRecording()
            StatusRecord.RESUME -> resumeRecording()
            else -> startRecording()
        }
    }

    private fun setUiRecording(status: StatusRecord) = binding.apply {
        if (status == StatusRecord.PAUSE) {
            imgRecording.loadImageDrawable(R.drawable.icon_micro)
            tvStatusRecord.text = getString(R.string.text_pause_recording)
        } else {
            imgRecording.loadImageDrawable(R.drawable.icon_micro_recording)
            tvStatusRecord.text = getString(R.string.text_recording)
        }
    }

    private fun startRecording() {
        viewModel.runTime()
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(
                FileOutputStream(
                    File(
                        fileUseCase.getOutputMediaDirectoryTemp(requireActivity()),
                        "${formatDate(FILE_NAME_FORMAT)}.mp4"
                    )
                ).fd
            )
            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
            recorder = this
        }
    }

    private fun resumeRecording() {
        viewModel.runTime()
        recorder?.resume()
    }

    private fun pauseRecording() {
        recorder?.pause()
        viewModel.stopTime()
    }

    private fun stopRecording() {
        if (recorder != null) {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            viewModel.stopTime()
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