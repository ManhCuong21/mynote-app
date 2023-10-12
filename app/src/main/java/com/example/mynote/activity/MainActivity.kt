package com.example.mynote.activity

import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import com.example.core.core.viewbinding.viewBinding
import com.notepad.mynote.privatenote.R
import com.notepad.mynote.privatenote.databinding.ActivityMainBinding


class MainActivity : BaseMainActivity() {
    private val binding by viewBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CrashlyticsLogger()
    }

    override val navHostFragmentActivityMain: FragmentContainerView
        get() = binding.navHostFragmentActivityMain
}