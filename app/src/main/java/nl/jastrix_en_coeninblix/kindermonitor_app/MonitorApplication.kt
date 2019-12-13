package nl.jastrix_en_coeninblix.kindermonitor_app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper


class MonitorApplication : Application() {
    var loginWithCachedCredentialsOnResume: Boolean = false
    //    var currentlySelectedPatient: Int? = null // should instead come from getsharedpreferences?
    var currentlyActiveActivity: Activity? = null
    val apiHelper = APIHelper()

    companion object {
        private var singleton: MonitorApplication? = null
        fun getInstance(): MonitorApplication {
            return singleton!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        singleton = this

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

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}