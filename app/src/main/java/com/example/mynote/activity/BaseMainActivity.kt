package com.example.mynote.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mynote.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseMainActivity : AppCompatActivity() {
    abstract val navHostFragmentActivityMain: FragmentContainerView

    private inline val navController: NavController
        get() = navHostFragmentActivityMain.getFragment<NavHostFragment>().navController

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
    }
}