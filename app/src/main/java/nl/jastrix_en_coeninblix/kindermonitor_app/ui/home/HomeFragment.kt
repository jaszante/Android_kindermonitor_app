package nl.jastrix_en_coeninblix.kindermonitor_app.ui.home

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Sensor
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class HomeFragment : Fragment() {

    companion object {
        var patientSensors: Array<Sensor>? = null
    }

    private var active = false

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentView = getView()

        Timer("schedule", false).scheduleAtFixedRate(1000, 1000) {
            if (patientSensors != null) {
                continuouslyCallForNewMeasurements()
            }
        }

        val vidstream = currentView!!.findViewById<VideoView>(R.id.videoStream)

        val vidAddress =
            "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"
        val vidUri: Uri = Uri.parse(vidAddress)
        vidstream.setVideoURI(vidUri)
        vidstream.start()
//        vidstream.isFocusable =false
//
//        val scrollView = view!!.findViewById(R.id.homeScrollView) as ScrollView
//        scrollView.isFocusableInTouchMode = true
//        scrollView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

        val myLayout = activity!!.findViewById(R.id.homeConstraintLayout) as ConstraintLayout
        myLayout.requestFocus()
    }

    override fun onResume() {
        super.onResume()

        active = true
    }

    override fun onStop() {
        super.onStop()

        active = false
    }

    private fun continuouslyCallForNewMeasurements() {
        // should do new call every second. do not use onresponse to call again, execute new call continuesly while app is in forefront, don't call at all while in background
        // if 5 calls in a row fail (every time a call succeeds it sets the counter to 0, every time it fails it checks the counter for 5 and ++'s that), send alarm that there is no connection with API or internet

        if (active) { // FOR EVERY VISUAL UPDATE, CHECK IF THIS FRAGMENT IS IN FOREGROUND

        }

//        val call = MainActivity.apiHelper.returnAPIServiceWithAuthenticationTokenAdded(
//            MainActivity.authToken
//        ).mes()
//        call.enqueue(object : Callback<UserData> {
//            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
//                val statusCode = response.code()
//
//                if (response.isSuccessful && response.body() != null){
//
//                }
//                else {
//                    if (statusCode == 401) {
//                        MainActivity.apiHelper.loginWithCachedUsernameAndPassword()
//                    }
//                    else if (statusCode == 404){
//                        // notification that there is no connection to API
//                    }
//                    else {
//                        // internet down notification
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<UserData>, t: Throwable) {
//                Log.d("DEBUG", t.message)
//
//                // internet down notification
//            }
//        })
    }
}