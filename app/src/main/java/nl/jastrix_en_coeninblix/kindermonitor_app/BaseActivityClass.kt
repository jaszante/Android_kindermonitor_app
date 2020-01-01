package nl.jastrix_en_coeninblix.kindermonitor_app

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


abstract class BaseActivityClass : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        MonitorApplication.getInstance().setCurrentActivity(this)
        MonitorApplication.getInstance().fragmentManager = supportFragmentManager
    }

//    override fun onPause() {
//        clearReferences()
//        super.onPause()
//    }

    override fun onDestroy() {
        clearReferences()
        super.onDestroy()
    }

    private fun clearReferences() {
        val currActivity = MonitorApplication.getInstance().getCurrentActivity()
        if (this == currActivity)
            MonitorApplication.getInstance().setCurrentActivity(null)
    }
}