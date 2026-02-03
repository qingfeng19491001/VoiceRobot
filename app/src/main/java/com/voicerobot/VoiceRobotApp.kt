package com.voicerobot

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.voicerobot.di.AppContainer
import java.io.PrintWriter
import java.io.StringWriter

class VoiceRobotApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val stack = sw.toString()
                Log.e("Crash", "Uncaught exception in thread=${t.name}: ${e.message}\n$stack")

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "崩溃: ${e.javaClass.simpleName}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Throwable) {
            }
        }

        container = AppContainer(this)
    }
}
