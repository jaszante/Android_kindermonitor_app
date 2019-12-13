package nl.jastrix_en_coeninblix.kindermonitor_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder


class ForegroundMeasurmentService: Service() {
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        // do continues measurement call

        return START_NOT_STICKY
    }
}