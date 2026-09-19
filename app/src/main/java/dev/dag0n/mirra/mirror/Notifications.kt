package dev.dag0n.mirra.mirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.dag0n.mirra.MainActivity
import dev.dag0n.mirra.R

object Notifications {
    const val CHANNEL_ID = "mirra_mirror"
    const val NOTIFICATION_ID = 41

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun build(context: Context, overlayVisible: Boolean): Notification {
        val open = pending(context, Intent(context, MainActivity::class.java), 1)
        val toggle = pending(
            context,
            Intent(context, MirrorService::class.java).setAction(MirrorService.ACTION_TOGGLE),
            2,
            service = true,
        )
        val stop = pending(
            context,
            Intent(context, MirrorService::class.java).setAction(MirrorService.ACTION_STOP),
            3,
            service = true,
        )
        val toggleLabel = if (overlayVisible) {
            context.getString(R.string.action_hide_overlay)
        } else {
            context.getString(R.string.action_show_overlay)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_mirra)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                if (overlayVisible) context.getString(R.string.notification_text_on)
                else context.getString(R.string.notification_text_off)
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .addAction(R.drawable.ic_stat_mirra, toggleLabel, toggle)
            .addAction(R.drawable.ic_stat_mirra, context.getString(R.string.action_stop), stop)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun pending(
        context: Context,
        intent: Intent,
        requestCode: Int,
        service: Boolean = false,
    ): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (service) {
            PendingIntent.getService(context, requestCode, intent, flags)
        } else {
            PendingIntent.getActivity(context, requestCode, intent, flags)
        }
    }
}
