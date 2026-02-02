package com.example.presentation.note.record

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.core.base.BaseFragment
import com.example.core.base.BaseViewModel
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.StatusRecord
import com.example.core.core.viewbinding.viewBinding
import com.example.domain.usecase.file.FileUseCase
import com.example.presentation.R
import com.example.presentation.canvas.Timer
import com.example.presentation.databinding.FragmentRecorderBinding
import com.example.presentation.dialog.permission.PermissionManager
import com.example.presentation.dialog.permission.PermissionRequest
import com.example.presentation.navigation.MainNavigator
import com.example.presentation.note.NoteFragment.Companion.RECORD_HAS
import com.example.presentation.note.NoteFragment.Companion.RECORD_RESULT
import dagger.hilt.android.AndroidEntryPoint
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

    @Inject
    lateinit var permissionManager: PermissionManager

    override val binding: FragmentRecorderBinding by viewBinding()
    override val viewModel: BaseViewModel
        get() = TODO("Not yet implemented")

    private var recorder: MediaRecorder? = null
    private lateinit var timer: Timer
    private lateinit var file: File
    val listPermission = listOf(
        PermissionRequest(permission = Manifest.permission.RECORD_AUDIO),
        PermissionRequest(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager.requestPermission(requests = listPermission) {}
    }

    override fun setupViews() {
        setupClickListener()
        setupTimer()
    }

    override fun bindViewModel() {
        // No TODO here
    }

    private fun setupTimer() = binding.apply {
        timer = Timer { time ->
            tvTimerRecord.text = time
        }
    }

    private var lastClickTime: Long = 0
    private fun setupClickListener() = binding.apply {
        var statusRecord = StatusRecord.CREATE
        btnRecording.setOnClickListener {
            if (abs(SystemClock.elapsedRealtime() - lastClickTime) > 1000) {
                permissionManager.requestPermission(requests = listPermission) {
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
            setFragmentResult(
                RECORD_RESULT,
                bundleOf(RECORD_HAS to true)
            )
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
            MediaRecorder(requireContext())
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
        timer.start()
        file = fileUseCase.createDirectoryTemp()
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(
                FileOutputStream(
                    File(
                        file,
                        "${System.currentTimeMillis()}.mp4"
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
        timer.start()
        recorder?.resume()
    }

    private fun pauseRecording() {
        recorder?.pause()
        timer.pause()
    }

    private fun stopRecording() {
        if (recorder != null) {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            timer.stop()
        }
    }
}