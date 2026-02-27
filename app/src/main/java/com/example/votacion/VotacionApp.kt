package com.example.votacion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VotacionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // global crash handler so we can see why the app is shutting down
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("VotacionApp", "Uncaught exception on thread ${thread.name}", throwable)
            // you could also write the stack trace to a file here for later inspection
        }
    }
}

