package nl.jastrix_en_coeninblix.kindermonitor_app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.fragment.app.FragmentManager
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.notifications.NotificationPopup
import nl.jastrix_en_coeninblix.kindermonitor_app.ui.home.HomeFragment
import java.lang.Exception
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class ForegroundMeasurmentService : Service() {
//    private lateinit var fragmentManager: FragmentManager

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

//    override fun onCreate() {
//        super.onCreate()
//        startForeground();
//    }

    private lateinit var timer: TimerTask

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
//        fragmentManager = MonitorApplication.getInstance().fragmentManager!!

        timer = Timer("schedule", false).scheduleAtFixedRate(0, 2000) {

            if (MonitorApplication.getInstance().stopMeasurementService){
                timer.cancel()
            }
            else{
                continuesMeasurementCall()
            }
        }

        return START_NOT_STICKY
    }

    private fun continuesMeasurementCall() {
        val monitorApplication = MonitorApplication.getInstance()
        var randomValue = (80..100).random()
        monitorApplication.hartslagLiveData.postValue(randomValue.toString())

        randomValue = (80..100).random()
        monitorApplication.temperatuurLiveData.postValue(randomValue.toString())

        randomValue = (80..100).random()
        monitorApplication.saturatieLiveData.postValue(randomValue.toString())

        randomValue = (80..100).random()
        monitorApplication.ademFrequentieLiveData.postValue(randomValue.toString())

//        // vervang randomValue met de 4 values van de api in final versie
//        // check grenswaarden
//        if (randomValue > 95 && monitorApplication.alarmNotPauzed) {
//            if (MonitorApplication.getInstance().fragmentManager!!.findFragmentByTag("Notification") == null){
//                val notificationPopup = NotificationPopup()
//
//                try {
//                    notificationPopup.show(MonitorApplication.getInstance().fragmentManager!!, "Notification")
//                    monitorApplication.startPauzeTimer()
//                }
//                finally {
//                    // means app is in background and last activity was destroyed, so pushnotification is the only notification that can pop up
//                }
//            }
//        }
    }

//    fun stopForegroundMeasurementService(){
//        stopForeground(true)
//        stopSelf()
//    }

//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceChannel = NotificationChannel(
//                CHANNEL_ID,
//                "Foreground Service Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(
//                NotificationManager::class.java
//            )
//            manager.createNotificationChannel(serviceChannel)
//        }
//    }
}