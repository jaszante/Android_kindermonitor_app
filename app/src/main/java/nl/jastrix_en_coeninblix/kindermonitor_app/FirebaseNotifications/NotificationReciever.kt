package nl.jastrix_en_coeninblix.kindermonitor_app.FirebaseNotifications

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication

class NotificationReciever : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("notification", "don't restart app pls, dumb default behaviour")
//        val mainActivityIntent = Intent(MonitorApplication.getInstance(), MainActivity::class.java)
//        mainActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
//        MonitorApplication.getInstance().startActivity(mainActivityIntent)
//        context.
    }
}