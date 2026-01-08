package com.example.mynote.activity

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import com.example.core.core.viewbinding.viewBinding
import com.notepad.mynote.privatenote.R
import com.notepad.mynote.privatenote.databinding.ActivityMainBinding

class MainActivity : BaseMainActivity() {
    private val binding by viewBinding<ActivityMainBinding>()

    private lateinit var motionDetector: MotionDetector
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CrashlyticsLogger()
        motionDetector = MotionDetector(this) {
            playAlertSound()
        }
    }

    private fun playAlertSound() {
        if (mediaPlayer?.isPlaying == true) return

        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)

        mediaPlayer?.let { player ->
            player.setOnCompletionListener {
                player.release()
                mediaPlayer = null
            }
            player.start()
        }
    }

    override fun onResume() {
        super.onResume()
        motionDetector.start()
    }

    override fun onPause() {
        super.onPause()
        motionDetector.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override val navHostFragmentActivityMain: FragmentContainerView
        get() = binding.navHostFragmentActivityMain
}