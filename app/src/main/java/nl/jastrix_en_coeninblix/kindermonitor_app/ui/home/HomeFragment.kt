package nl.jastrix_en_coeninblix.kindermonitor_app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userData
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Sensor
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

//        Timer("schedule", false).scheduleAtFixedRate(1000, 1000) {
//            continuouslyCallForNewMeasurements()
//        }

        return root
    }

    private fun continuouslyCallForNewMeasurements() {
        // should do new call every second. do not use onresponse to call again, execute new call continuesly while app is in forefront, don't call at all while in background
        // if 5 calls in a row fail (every time a call succeeds it sets the counter to 0, every time it fails it checks the counter for 5 and ++'s that), send alarm that there is no connection with API or internet


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