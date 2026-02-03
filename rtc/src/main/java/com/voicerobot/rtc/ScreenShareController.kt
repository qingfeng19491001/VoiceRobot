package com.voicerobot.rtc

import android.app.Application
import android.content.Intent
import android.os.Build
import com.ss.bytertc.base.media.screen.RXScreenCaptureService

class ScreenShareController(
    private val app: Application,
) {
    fun startForegroundService(resultData: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val ctx = app.applicationContext
            val intent = Intent().apply {
                putExtra(
                    RXScreenCaptureService.KEY_LAUNCH_ACTIVITY,
                    app.packageManager.getLaunchIntentForPackage(app.packageName)?.component?.className
                )
                putExtra(RXScreenCaptureService.KEY_CONTENT_TEXT, "正在录制/投射您的屏幕")
                putExtra(RXScreenCaptureService.KEY_RESULT_DATA, resultData)
            }
            ctx.startForegroundService(
                RXScreenCaptureService.getServiceIntent(ctx, RXScreenCaptureService.COMMAND_LAUNCH, intent)
            )
        }
    }

    fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val ctx = app.applicationContext
            ctx.startForegroundService(
                RXScreenCaptureService.getServiceIntent(ctx, RXScreenCaptureService.COMMAND_STOP, null)
            )
        }
    }
}
