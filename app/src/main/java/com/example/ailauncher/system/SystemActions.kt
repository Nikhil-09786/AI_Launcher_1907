package com.example.ailauncher.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.widget.Toast
import java.util.Locale

object SystemActions {

    fun makePhoneCall(context: Context, target: String): String {
        return try {
            val cleanTarget = target.replace(Regex("[^0-9+]"), "")
            val numberUri = if (cleanTarget.isNotEmpty()) cleanTarget else target
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$numberUri")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Initiated call dialer for '$target'"
        } catch (e: Exception) {
            "Failed to open dialer: ${e.localizedMessage}"
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String): String {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val targetNum = if (cleanNumber.isNotEmpty()) cleanNumber else phoneNumber
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$targetNum")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Opening SMS composer for '$phoneNumber'"
        } catch (e: Exception) {
            "Failed to open SMS composer: ${e.localizedMessage}"
        }
    }

    fun setAlarm(context: Context, hour: Int, minute: Int, label: String): String {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label.ifEmpty { "AI Alarm" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            val timeFormatted = String.format(Locale.US, "%02d:%02d", hour, minute)
            "Alarm set for $timeFormatted ${if (label.isNotEmpty()) "($label)" else ""}"
        } catch (e: Exception) {
            // Fallback without skip UI flag if permission missing
            try {
                val fallbackIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(AlarmClock.EXTRA_MESSAGE, label.ifEmpty { "AI Alarm" })
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                "Opened alarm clock for $hour:$minute"
            } catch (err: Exception) {
                "Failed to set alarm: ${err.localizedMessage}"
            }
        }
    }

    fun openApp(context: Context, appName: String): String {
        val pm = context.packageManager
        val cleanAppName = appName.lowercase(Locale.ROOT).trim()

        // Common package mappings for instant lookup
        val directPackageMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "camera" to "com.android.camera",
            "gallery" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "settings" to "com.android.settings",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "instagram" to "com.instagram.android",
            "spotify" to "com.spotify.music",
            "telegram" to "org.telegram.messenger"
        )

        val directPackage = directPackageMap[cleanAppName]
        if (directPackage != null) {
            val intent = pm.getLaunchIntentForPackage(directPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return "Opening $appName..."
            }
        }

        // Search through all installed applications by label
        return try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            for (info in resolveInfos) {
                val label = info.loadLabel(pm).toString().lowercase(Locale.ROOT)
                if (label.contains(cleanAppName) || cleanAppName.contains(label)) {
                    val pkgName = info.activityInfo.packageName
                    val launchIntent = pm.getLaunchIntentForPackage(pkgName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return "Opening ${info.loadLabel(pm)}..."
                    }
                }
            }

            "Could not find installed app matching '$appName'"
        } catch (e: Exception) {
            "Failed to launch app: ${e.localizedMessage}"
        }
    }
}
