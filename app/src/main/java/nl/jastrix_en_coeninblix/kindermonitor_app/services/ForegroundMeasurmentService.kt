package nl.jastrix_en_coeninblix.kindermonitor_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Measurement
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientSensor
import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType
import nl.jastrix_en_coeninblix.kindermonitor_app.notifications.NoConnectionPopup
import nl.jastrix_en_coeninblix.kindermonitor_app.notifications.NotificationPopup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class ForegroundMeasurmentService : Service() {
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    private lateinit var timer: TimerTask

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
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

        requestMeasurementFromSensor(monitorApplication, monitorApplication.temperatuurSensor!!)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.hartslagSensor!!)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.ademFrequentieSensor!!)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.saturatieSensor!!)
    }

    private fun requestMeasurementFromSensor(monitorApplication: MonitorApplication, patientSensor: PatientSensor){
        val call = monitorApplication.apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .getMeasurementsForSensor(patientSensor.sensorID)
        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()!!.count() != 0) {
                        val responseBody = response.body()!![response.body()!!.count() - 1]
//                    val monitorApplication = MonitorApplication.getInstance()
                        val newValue =
                            BigDecimal(responseBody.value).setScale(1, RoundingMode.HALF_EVEN)
                                .toString()
                        when (patientSensor.sensorType) {
                            SensorType.Hartslag -> {

                                monitorApplication.hartslagLiveData.postValue(newValue.toString())

                                if (responseBody.value < monitorApplication.hartslagSensor!!.thresholdMin) {
                                    thresholdPopup("Hartslag is te laag " + newValue)
                                    monitorApplication.hartslagLayoutLiveData.value = false
                                } else if (responseBody.value
                                    > monitorApplication.hartslagSensor!!.thresholdMax
                                ) {
                                    thresholdPopup("Hartslag is te hoog " + newValue)
                                    monitorApplication.hartslagLayoutLiveData.value = false
                                } else if (monitorApplication.hartslagLayoutLiveData.value == false) {
                                    monitorApplication.hartslagLayoutLiveData.value = true
                                }
                            }
                            SensorType.Temperature -> {
                                monitorApplication.temperatuurLiveData.postValue(newValue.toString())
                                if (responseBody.value < monitorApplication.temperatuurSensor!!.thresholdMin) {
                                    thresholdPopup("Temperatuur is te laag " + newValue)
                                    monitorApplication.temperatuurLayoutLiveData.value = false
                                } else if (responseBody.value
                                    > monitorApplication.temperatuurSensor!!.thresholdMax
                                ) {
                                    thresholdPopup("Temperatuur is te hoog " + newValue)
                                    monitorApplication.temperatuurLayoutLiveData.value = false
                                } else if (monitorApplication.temperatuurLayoutLiveData.value == false) {
                                    monitorApplication.temperatuurLayoutLiveData.value = true
                                }
                            }
                            SensorType.Adem -> {
                                monitorApplication.ademFrequentieLiveData.postValue(newValue.toString())
                                if (responseBody.value < monitorApplication.ademFrequentieSensor!!.thresholdMin) {
                                    thresholdPopup("Adem frequentie is te laag " + newValue)
                                    monitorApplication.ademFrequentieLayoutLiveData.value = false
                                } else if (responseBody.value
                                    > monitorApplication.ademFrequentieSensor!!.thresholdMax
                                ) {
                                    thresholdPopup("Adem frequentie is te hoog " + newValue)
                                    monitorApplication.ademFrequentieLayoutLiveData.value = false
                                } else if (monitorApplication.ademFrequentieLayoutLiveData.value == false) {
                                    monitorApplication.ademFrequentieLayoutLiveData.value = true
                                }
                            }
                            SensorType.Saturatie -> {
                                monitorApplication.saturatieLiveData.postValue(newValue.toString())
                                if (responseBody.value < monitorApplication.saturatieSensor!!.thresholdMin) {
                                    thresholdPopup("Saturatie is te laag " + newValue)
                                    monitorApplication.saturatieLayoutLiveData.value = false
                                } else if (responseBody.value
                                    > monitorApplication.saturatieSensor!!.thresholdMax
                                ) {
                                    thresholdPopup("Saturatie is te hoog " + newValue)
                                    monitorApplication.saturatieLayoutLiveData.value = false
                                } else if (monitorApplication.saturatieLayoutLiveData.value == false) {
                                    monitorApplication.saturatieLayoutLiveData.value = true
                                }
                            }
                        }
                    }
                    else
                    {
                        // means no measurments are in the API, should only happen when a new patient has just been created in the app and no measurments are sent to the API yet
                    }

                } else {
                    thresholdPopup(response.message())
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                noConnectionPopup(getString(R.string.noInternetError)) //t.message!!)
            }
        })
    }

    private fun thresholdPopup(message: String){
        if (MonitorApplication.getInstance().fragmentManager!!.findFragmentByTag("Notification") == null
            && MonitorApplication.getInstance().alarmNotPauzed && MonitorApplication.getInstance().appInForeground){
                val notificationPopup = NotificationPopup(message)

                try {
                    notificationPopup.show(MonitorApplication.getInstance().fragmentManager!!, "Notification")
                    MonitorApplication.getInstance().currentlyShowingErrorColor = true
                }
                finally {
                    // means app is in background and last activity was destroyed, so pushnotification is the only notification that can pop up
                }
            }
    }

    private fun noConnectionPopup(message: String){
        if (MonitorApplication.getInstance().fragmentManager!!.findFragmentByTag("NoConnectionNotification") == null &&
                MonitorApplication.getInstance().noConnectionAlarmNotPauzed && MonitorApplication.getInstance().appInForeground){
            val noConnectionPopup = NoConnectionPopup(message)

            try {
                noConnectionPopup.show(MonitorApplication.getInstance().fragmentManager!!, "NoConnectionNotification")
            }
            finally {
                // means app is in background and last activity was destroyed, so pushnotification is the only notification that can pop up
            }
        }
    }


//    fun stopForegroundMeasurementService(){
//        stopForeground(true)
//        stopSelf()
//    }
}