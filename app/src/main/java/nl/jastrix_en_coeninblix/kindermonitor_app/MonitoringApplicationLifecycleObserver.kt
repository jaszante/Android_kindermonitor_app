package nl.jastrix_en_coeninblix.kindermonitor_app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MonitoringApplicationLifecycleObserver : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onForeground() {
            // App goes to foreground

            MonitorApplication.getInstance().appInForeground = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onBackground() {
            // App goes to background

            MonitorApplication.getInstance().appInForeground = false
        }

}