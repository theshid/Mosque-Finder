package com.shid.mosquefinder.app.ui.main.view_models

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.ui.broadcast_receiver.PrayerAlarmBroadcastReceiver
import com.shid.mosquefinder.app.utils.extensions.showToast
import com.shid.mosquefinder.app.utils.hour
import com.shid.mosquefinder.app.utils.minutes
import de.coldtea.smplr.smplralarm.alarmNotification
import de.coldtea.smplr.smplralarm.channel
import de.coldtea.smplr.smplralarm.smplrAlarmCancel
import de.coldtea.smplr.smplralarm.smplrAlarmSet


class PrayerViewModel : ViewModel() {

    fun setPrayerAlarm(
        context: Context,
        prayerType: String,
        prayer: String,
        showToast: Boolean? = true
    ): Int {
        val alarmReceivedIntent = Intent(
            context,
            PrayerAlarmBroadcastReceiver::class.java
        )
        if (showToast == true) context.showToast(buildString {
            append(context.getString(R.string.prayer_toast))
            append(prayer)
            append(context.getString(R.string.prayer_toast1))
            append(prayerType)
            append(context.getString(R.string.prayer_toast2))
        })

        return smplrAlarmSet(context) {
            hour { prayerType.hour }
            min { prayerType.minutes }
            weekdays {
                monday()
                tuesday()
                wednesday()
                thursday()
                friday()
                saturday()
                sunday()
            }
            alarmReceivedIntent { alarmReceivedIntent }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.logo2 }
                    title { context.getString(R.string.notification_title_) }
                    message {
                        buildString {
                            append(context.getString(R.string.notification_msg1))
                            append(prayer)
                            append(context.getString(R.string.notification_msg2))
                            append(prayerType)
                        }
                    }
                    bigText {
                        buildString {
                            append(context.getString(R.string.notification_msg1))
                            append(prayer)
                            append(context.getString(R.string.notification_msg2))
                            append(prayerType)
                        }
                    }
                    autoCancel { true }

                }
            }
            notificationChannel {
                channel {
                    importance { NotificationManager.IMPORTANCE_HIGH }
                    showBadge { false }
                    name { "com.shid.smplr.mosquefinder.channel" }
                    description { "Mosque Finder notification channel" }

                }
            }
        }
    }

    fun cancelPrayerNotification(requestCode: Int, context: Context, prayerName: String) {
        context.showToast(buildString {
            append(context.getString(R.string.prayer_toast))
            append(prayerName)
            append(context.getString(R.string.prayer_toast3))
        })
        smplrAlarmCancel(context) {
            requestCode { requestCode }
        }
    }

    /*private fun getScheduleName(index: Int) = when (index) {
        0 -> Common.FAJR
        1 -> Common.DHUR
        2 -> Common.ASR
        3 -> Common.MAGHRIB
        4 -> Common.ISHA
        else -> "-"
    }*/
}