package nl.jastrix_en_coeninblix.kindermonitor_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Measurement
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientSensor
import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType
import nl.jastrix_en_coeninblix.kindermonitor_app.notifications.NotificationPopup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
//        var randomValue = (80..100).random()

//        if (monitorApplication.patientSensors.count() > 0) {
//            for (patientSensor in monitorApplication.patientSensors){
//                requestMeasurementFromSensor(monitorApplication, patientSensor.sensorID, patientSensor.sensorType)
//            }
//        }

        requestMeasurementFromSensor(monitorApplication, monitorApplication.temperatuurSensor)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.hartslagSensor)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.ademFrequentieSensor)
        requestMeasurementFromSensor(monitorApplication, monitorApplication.saturatieSensor)

//        monitorApplication.hartslagLiveData.postValue(randomValue.toString())
//
//        randomValue = (80..100).random()
//        monitorApplication.temperatuurLiveData.postValue(randomValue.toString())
//
//        randomValue = (80..100).random()
//        monitorApplication.saturatieLiveData.postValue(randomValue.toString())
//
//        randomValue = (80..100).random()
//        monitorApplication.ademFrequentieLiveData.postValue(randomValue.toString())

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

    private fun requestMeasurementFromSensor(monitorApplication: MonitorApplication, patientSensor: PatientSensor){
        val call = monitorApplication.apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .getMeasurementsForSensor(patientSensor.sensorID)
        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                if (response.isSuccessful && response.body() != null && response.body()!!.count() != 0) {
                    val responseBody = response.body()!![response.body()!!.count() - 1]
//                    val monitorApplication = MonitorApplication.getInstance()

                    when(patientSensor.sensorType){
                        SensorType.Hartslag -> {
                            monitorApplication.hartslagLiveData.postValue(responseBody.value.toString())
                            if (responseBody.value > monitorApplication.hartslagSensor.thresholdMin){
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                            if (responseBody.value
                                < monitorApplication.hartslagSensor.thresholdMax)
                            {
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                        }
                        SensorType.Temperature -> {
                            monitorApplication.temperatuurLiveData.postValue(responseBody.value.toString())
                            if (responseBody.value > monitorApplication.temperatuurSensor.thresholdMin){
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                            if (responseBody.value
                                < monitorApplication.temperatuurSensor.thresholdMax)
                            {
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                        }
                        SensorType.Adem -> {
                            monitorApplication.ademFrequentieLiveData.postValue(responseBody.value.toString())
                            if (responseBody.value > monitorApplication.ademFrequentieSensor.thresholdMin){
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                            if (responseBody.value
                                < monitorApplication.ademFrequentieSensor.thresholdMax)
                            {
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                        }
                        SensorType.Saturatie -> {
                            monitorApplication.saturatieLiveData.postValue(responseBody.value.toString())
                            if (responseBody.value > monitorApplication.saturatieSensor.thresholdMin){
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                            if (responseBody.value
                                < monitorApplication.saturatieSensor.thresholdMax)
                            {
                                thresholdOrNoConnectionPopup("iets idk")
                            }
                        }
                    }

                } else {
                    thresholdOrNoConnectionPopup(response.message())
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                thresholdOrNoConnectionPopup(t.message!!)
            }
        })
    }

//    private fun requestMeasurmentFromSensor(monitorApplication: MonitorApplication, id: Int): ArrayList<Measurement> {
//        val call = monitorApplication.apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getMeasurementsForSensor(id)
//
//        call.enqueue(object : Callback<ArrayList<Measurement>> {
//            override fun onResponse(call: Call<ArrayList<Measurement>>, response: Response<ArrayList<Measurement>>) {
//                if (response.isSuccessful && response.body() != null) {
//                    response.body()!!
//
//                    registerPatientButton.isClickable = true
//
//                } else {
//                    noConnectionError(response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<ArrayList<Measurement>>, t: Throwable) {
//                noConnectionError(t.message!!)
//            }
//        })
//    }

    private fun thresholdOrNoConnectionPopup(message: String){
        if (MonitorApplication.getInstance().fragmentManager!!.findFragmentByTag("Notification") == null){
                val notificationPopup = NotificationPopup()
//                notificationPopup.dialog // should set text here to message
//            notificationPopup. doe hier methode die text veranderd binnen de popupclass

                try {
                    notificationPopup.show(MonitorApplication.getInstance().fragmentManager!!, "Notification")
                    MonitorApplication.getInstance().startPauzeTimer()
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