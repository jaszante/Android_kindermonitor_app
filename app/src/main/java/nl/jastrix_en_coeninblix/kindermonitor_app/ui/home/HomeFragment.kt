package nl.jastrix_en_coeninblix.kindermonitor_app.ui.home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_home.*
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.SensorFromCallback
//import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity.Companion.loginWithCachedCredentialsOnResume


class HomeFragment : Fragment() {

//    companion object {
//        var patientSensors: Array<SensorFromCallback>? = null
//    }

    private var active = false

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var hartslagValue: TextView
    private lateinit var temperatuurValue: TextView
    private lateinit var ademFrequetieValue: TextView
    private lateinit var saturatieValue: TextView

    private lateinit var hartslagLayout: LinearLayout
    private lateinit var temperatuurLayout: LinearLayout
    private lateinit var ademFrequentieLayout: LinearLayout
    private lateinit var saturatieLayout: LinearLayout

    lateinit var hartslagGrensWaardes: TextView
    lateinit var temperatuurGrensWaardes: TextView
    lateinit var ademfrequentieGrensWaardes: TextView
    lateinit var saturatieGrensWaardes: TextView

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

        hartslagLayout = currentView.findViewById(R.id.linearLayoutHartslag)
        temperatuurLayout = currentView.findViewById(R.id.linearLayoutTemperatuur)
        ademFrequentieLayout = currentView.findViewById(R.id.linearLayoutAdemFrequentie)
        saturatieLayout = currentView.findViewById(R.id.linearLayoutSaturatie)

        hartslagGrensWaardes = currentView.findViewById(R.id.hartslagGrensWaardes)
        temperatuurGrensWaardes = currentView.findViewById(R.id.temperatuurGrensWaardes)
        ademfrequentieGrensWaardes = currentView.findViewById(R.id.ademfrequentieGrensWaarden)
        saturatieGrensWaardes = currentView.findViewById(R.id.saturatieGrensWaardes)


        val vidstream = currentView.findViewById<VideoView>(R.id.videoStream)

        val vidAddress =
            "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"
        val vidUri: Uri = Uri.parse(vidAddress)
        vidstream.setVideoURI(vidUri)
        vidstream.start()

        val myLayout = activity!!.findViewById(R.id.homeConstraintLayout) as ConstraintLayout
        myLayout.requestFocus()
    }

    private val changeHartslagLiveDataObserver = Observer<String> {
            value ->
        value?.let { hartslagValue.text = it }
    }

    private val changeTemperatuurLiveDataObserver = Observer<String> { value ->
        value?.let { temperatuurValue.text = it }
    }

    private val changeSaturatieLiveDataObserver = Observer<String> { value ->
        value?.let { saturatieValue.text = it }
    }

    private val changeAdemfrequentieLiveDataObserver = Observer<String> { value ->
        value?.let { ademFrequetieValue.text = it }
    }

    private val changeHartslagColor = Observer<Boolean> {
            value ->
        value?.let {
            if (it) {
                hartslagLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGood))
            }
            else{
                hartslagLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBad))
            }
        }
    }

    private val changeTemperatuurColor = Observer<Boolean> {
            value ->
        value?.let {
            if (it) {
                temperatuurLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGood))
            }
            else{
                temperatuurLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBad))
            }
        }
    }

    private val changeAdemFrequentieColor = Observer<Boolean> {
            value ->
        value?.let {
            if (it) {
                ademFrequentieLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGood))
            }
            else{
                ademFrequentieLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBad))
            }
        }
    }

    private val changeSaturatieColor = Observer<Boolean> {
            value ->
        value?.let {
            if (it) {
                saturatieLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorGood))
            }
            else{
                saturatieLayout.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorBad))
            }
        }
    }

    private val changeHartslagThresholds = Observer<String> {
            value ->
        value?.let {
            hartslagGrensWaardes.text = it
        }
    }

    private val changeTemperatuurThresholds = Observer<String> {
            value ->
        value?.let {
            temperatuurGrensWaardes.text = it
        }
    }

    private val changeAdemFrequentieThresholds = Observer<String> {
            value ->
        value?.let {
            ademfrequentieGrensWaardes.text = it
        }
    }

    private val changeSaturatieThresholds = Observer<String> {
            value ->
        value?.let {
            saturatieGrensWaardes.text = it
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val monitorApplication = MonitorApplication.getInstance()

        monitorApplication.hartslagLiveData.observe(this, changeHartslagLiveDataObserver)
        monitorApplication.temperatuurLiveData.observe(this, changeTemperatuurLiveDataObserver)
        monitorApplication.saturatieLiveData.observe(this, changeSaturatieLiveDataObserver)
        monitorApplication.ademFrequentieLiveData.observe(this, changeAdemfrequentieLiveDataObserver)

        monitorApplication.hartslagLayoutLiveData.observe(this, changeHartslagColor)
        monitorApplication.temperatuurLayoutLiveData.observe(this, changeTemperatuurColor)
        monitorApplication.ademFrequentieLayoutLiveData.observe(this, changeAdemFrequentieColor)
        monitorApplication.saturatieLayoutLiveData.observe(this, changeSaturatieColor)

        monitorApplication.hartslagThresholds.observe(this, changeHartslagThresholds)
        monitorApplication.temperatuurThresholds.observe(this, changeTemperatuurThresholds)
        monitorApplication.ademfrequentieThresholds.observe(this, changeAdemFrequentieThresholds)
        monitorApplication.saturatieThresholds.observe(this, changeSaturatieThresholds)
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

//    private fun continuouslyCallForNewMeasurements() {
//        // should do new call every second. do not use onresponse to call again, execute new call continuesly while app is in forefront, don't call at all while in background
//        // if 5 calls in a row fail (every time a call succeeds it sets the counter to 0, every time it fails it checks the counter for 5 and ++'s that), send alarm that there is no connection with API or internet
//
//        val loginIntent = Intent(activity, LoginActivity::class.java)
//        val call =
//            MonitorApplication.getInstance().apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getMeasurementsForSensor(
//                patientSensors!![0].sensorID
//            )
//        call.enqueue(object : Callback<Array<Measurement>> {
//            override fun onResponse(
//                call: Call<Array<Measurement>>,
//                response: Response<Array<Measurement>>
//            ) {
//                val statusCode = response.code()
//
//                if (response.isSuccessful && response.body() != null) {
//                    val test = response.body()
//                    if (active) { // FOR EVERY VISUAL UPDATE, CHECK IF THIS FRAGMENT IS IN FOREGROUND
//
//                    }
//                } else {
//                    if (statusCode == 401) {
//                        MonitorApplication.getInstance().loginWithCachedCredentialsOnResume = true
//                        startActivity(loginIntent)
//                    } else if (statusCode == 404) {
//                        // notification that there is no connection to API
//                    } else {
//                        // internet down notification
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
//                Log.d("DEBUG", t.message)
//
//                // internet down notification
//            }
//        })
//    }
}