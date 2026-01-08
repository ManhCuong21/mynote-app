package com.example.mynote

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        try {
            System.loadLibrary("opencv_java4")
            Timber.d("Loaded OpenCV native libs ✅")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e("Failed to load OpenCV libs: + $e")
        }
    }
}