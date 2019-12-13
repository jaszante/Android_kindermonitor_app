package nl.jastrix_en_coeninblix.kindermonitor_app.ui.home

import android.content.Intent
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
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Measurement
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.MeasurementForPost
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.SensorFromCallback
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity.Companion.loginWithCachedCredentialsOnResume
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate


class HomeFragment : Fragment() {

    companion object {
        var patientSensors: Array<SensorFromCallback>? = null
    }

    private var active = false

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var hartslagValue: TextView
    private lateinit var temperatuurValue: TextView
    private lateinit var ademFrequetieValue: TextView
    private lateinit var saturatieValue: TextView

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
        val currentView = getView()!!

        hartslagValue = currentView.findViewById(R.id.hartslagValue)
        temperatuurValue = currentView.findViewById(R.id.temperatuurValue)
        ademFrequetieValue = currentView.findViewById(R.id.ademFrequentieValue)
        saturatieValue = currentView.findViewById(R.id.saturatieValue)

        val vidstream = currentView.findViewById<VideoView>(R.id.videoStream)

        val vidAddress =
            "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"
        val vidUri: Uri = Uri.parse(vidAddress)
        vidstream.setVideoURI(vidUri)
        vidstream.start()

        val myLayout = activity!!.findViewById(R.id.homeConstraintLayout) as ConstraintLayout
        myLayout.requestFocus()

        Timer("schedule", false).scheduleAtFixedRate(0, 3000) {
            if (patientSensors != null) {
                var randomValue = (80..100).random()
                hartslagValue.text = randomValue.toString()

                randomValue = (80..100).random()
                temperatuurValue.text = randomValue.toString()

                randomValue = (80..100).random()
                ademFrequetieValue.text = randomValue.toString()

                randomValue = (80..100).random()
                saturatieValue.text = randomValue.toString()
            }
        }

//        Timer("schedule", false).scheduleAtFixedRate(0, 3000) {
//            if (patientSensors != null) {
////                continuouslyPostNewMeasurements()
//                continuouslyCallForNewMeasurements()
//            }
//        }

//        Timer("schedule", false).schedule(6000) {
//            if (patientSensors != null) {
//                continuouslyCallForNewMeasurements()
//            }
//        }

    }

    override fun onResume() {
        super.onResume()

        active = true
    }

    override fun onStop() {
        super.onStop()

        active = false
    }

//    private fun continuouslyPostNewMeasurements() {
//        val call = apiHelper.returnAPIServiceWithAuthenticationTokenAdded().postMeasurementToSensor(
//            patientSensors!![0].sensorID, MeasurementForPost(70)
//        )
//        call.enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                val statusCode = response.code()
//
//                if (response.isSuccessful && response.body() != null){
//                    val test = response.body()
//                }
//                else {
//                    // absolutetly nothing, this function shouldn't even be part of the app.
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Log.d("DEBUG", t.message)
//
//                // internet down notification
//            }
//        })
//    }

    private fun continuouslyCallForNewMeasurements() {
        // should do new call every second. do not use onresponse to call again, execute new call continuesly while app is in forefront, don't call at all while in background
        // if 5 calls in a row fail (every time a call succeeds it sets the counter to 0, every time it fails it checks the counter for 5 and ++'s that), send alarm that there is no connection with API or internet

        val loginIntent = Intent(activity, LoginActivity::class.java)
        val call =
            MonitorApplication.getInstance().apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getMeasurementsForSensor(
                patientSensors!![0].sensorID
            )
        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    val test = response.body()
                    if (active) { // FOR EVERY VISUAL UPDATE, CHECK IF THIS FRAGMENT IS IN FOREGROUND

                    }
                } else {
                    if (statusCode == 401) {
                        MonitorApplication.getInstance().loginWithCachedCredentialsOnResume = true
                        startActivity(loginIntent)
                    } else if (statusCode == 404) {
                        // notification that there is no connection to API
                    } else {
                        // internet down notification
                    }
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                Log.d("DEBUG", t.message)

                // internet down notification
            }
        })
    }
}