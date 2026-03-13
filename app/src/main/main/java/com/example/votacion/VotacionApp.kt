package com.example.votacion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VotacionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("VotacionApp", "Uncaught exception on thread ${thread.name}", throwable)
        }
    }
}

