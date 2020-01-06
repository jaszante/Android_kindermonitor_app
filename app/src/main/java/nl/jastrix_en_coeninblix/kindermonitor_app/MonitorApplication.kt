package nl.jastrix_en_coeninblix.kindermonitor_app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientSensor
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.ThresholdValues
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.services.ForegroundMeasurmentService
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

class MonitorApplication : Application() {
    //    var currentlySelectedPatient: Int? = null // should instead come from getsharedpreferences?
    var currentlyActiveActivity: Activity? = null
    val apiHelper = APIHelper()
    var currentlySelectedPatient: PatientWithID? = null
    var userName: String? = null
    var password: String? = null
    var authToken: String = ""
    var userData: UserData? = null
    var authTokenChanged = false
    var fragmentManager: FragmentManager? = null
    var hartslagLiveData = MutableLiveData<String>()
    var saturatieLiveData = MutableLiveData<String>()
    var temperatuurLiveData = MutableLiveData<String>()
    var ademFrequentieLiveData = MutableLiveData<String>()
    var pauzeTime: Long = 10000
    lateinit var hartslagSensor: PatientSensor
    lateinit var temperatuurSensor: PatientSensor
    lateinit var ademFrequentieSensor: PatientSensor
    lateinit var saturatieSensor: PatientSensor

    var stopMeasurementService: Boolean = false

    var alarmNotPauzed = true

    private var currentActivity: Activity? = null

//    var patientSensors: ArrayList<PatientSensor> = ArrayList()

    companion object {
        private var singleton: MonitorApplication? = null
        fun getInstance(): MonitorApplication {
            return singleton!!
        }
    }

    fun startPauzeTimer() {
        Timer("schedule", false).schedule(pauzeTime) {
            alarmNotPauzed = true
        }
    }

    fun startForegroundMeasurmentService() {
        val foregroundMeasurmentService = Intent(this, ForegroundMeasurmentService::class.java)
        startService(foregroundMeasurmentService)
    }

    fun setCurrentActivity(newCurrentActivity: Activity?){
        currentActivity = newCurrentActivity
    }

    fun getCurrentActivity(): Activity?{
        return currentActivity
    }

    override fun onCreate() {
        super.onCreate()

        singleton = this

//        setupLifecycleListener()
//        //https://medium.com/@mohitsharma_49363/android-detect-app-foreground-time-9b4f6752b077
//        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
//            override fun onActivityCreated(activity: Activity, bundle: Bundle) {}
//            override fun onActivityStarted(activity: Activity) {
//                currentlyActiveActivity = activity
//            }
//
//            override fun onActivityResumed(activity: Activity) {
//                currentlyActiveActivity = activity
//            }
//
//            override fun onActivityPaused(activity: Activity) {
//                currentlyActiveActivity = null
//            }
//
//            override fun onActivityStopped(activity: Activity) {}
//            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
//            override fun onActivityDestroyed(activity: Activity) {}
//        })
    }

//    private val lifecycleListener: SampleLifecycleListener by lazy {
//        SampleLifecycleListener()
//    }
//
//    private fun setupLifecycleListener() {
//        ProcessLifecycleOwner.get().lifecycle
//            .addObserver(lifecycleListener)
//    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}