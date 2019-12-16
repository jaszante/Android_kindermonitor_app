package nl.jastrix_en_coeninblix.kindermonitor_app.services

import android.app.Notification
import android.app.Service
import android.app.TimePickerDialog
import android.content.Intent
import android.os.IBinder
import android.widget.TimePicker
import androidx.fragment.app.FragmentManager
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.notifications.NotificationPopup
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.scheduleAtFixedRate


class ForegroundMeasurmentService: Service() {
    private lateinit var fragmentManager: FragmentManager

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        fragmentManager = MonitorApplication.getInstance().fragmentManager!!

        Timer("schedule", false).scheduleAtFixedRate(0, 2000) {
            continuesMeasurementCall()
        }

        return START_NOT_STICKY
    }

    private fun continuesMeasurementCall(){

        // on error:
//        val notificationPopup = NotificationPopup()
//        notificationPopup.show(fragmentManager, "no")
    }
}