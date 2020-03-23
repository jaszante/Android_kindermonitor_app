package nl.jastrix_en_coeninblix.kindermonitor_app.ui.home

//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R


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


        // voorbeeld livestream in de echte app zou dit een andere player zijn
        val vidstream = currentView.findViewById<YouTubePlayerView>(R.id.videoStream)
        lifecycle.addObserver(vidstream)

        vidstream.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "5qap5aO4i9A"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })






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
}