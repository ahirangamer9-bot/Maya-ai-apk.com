package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.controller.PhoneController

class MayaWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_FLASH = "com.example.ACTION_TOGGLE_FLASH"
        const val EXTRA_TARGET_TAB = "target_tab"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_FLASH) {
            PhoneController.toggleTorch(context)
            PhoneController.triggerHaptic(context, 80)

            // Refresh all widgets
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MayaWidgetProvider::class.java))
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.maya_widget_layout)

        // 1. Open App Intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_open, openPendingIntent)

        // 2. Chat Button Intent
        val chatIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, "Chat")
        }
        val chatPendingIntent = PendingIntent.getActivity(
            context,
            1,
            chatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_chat, chatPendingIntent)

        // 3. Voice Button Intent
        val voiceIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, "Voice")
        }
        val voicePendingIntent = PendingIntent.getActivity(
            context,
            2,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_voice, voicePendingIntent)

        // 4. Flashlight Toggle Intent
        val flashIntent = Intent(context, MayaWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_FLASH
        }
        val flashPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            flashIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_flash, flashPendingIntent)

        // Update flash button text based on torch state
        val isTorchOn = PhoneController.isTorchActive()
        views.setTextViewText(R.id.btn_widget_flash, if (isTorchOn) "🔦 Off" else "🔦 Torch")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
