package nl.jastrix_en_coeninblix.kindermonitor_app.ui.gallery

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_slideshow.*
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.FromTo
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Measurement
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.graphPage.GraphPage
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterPatientActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var fromDate: EditText
    private lateinit var toDate: EditText
    private lateinit var btnGraph: Button
    private lateinit var graph: GraphView
    var fromDateString: String = ""
    var toDateString: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProviders.of(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentView = getView()!!
        graph = currentView.findViewById<GraphView>(R.id.graph)
        btnGraph = currentView.findViewById(R.id.btn_Graph)

        fromDate = currentView.findViewById(R.id.fromDate)
        toDate = currentView.findViewById(R.id.toDate)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var month2 = month + 1
        fromDateString = "" + month2 + "-" + day + "-" + year
        toDateString = "" + month2 + "-" + day + "-" + year

        val dpd = DatePickerDialog(
            this.context,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                fromDate.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + year)
                fromDateString = "" + monthPlusOne + "-" + dayOfMonth + "-" + year
            },
            year,
            month,
            day
        )
        val dpd2 = DatePickerDialog(
            this.context,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                toDate.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + year)
                toDateString = "" + monthPlusOne + "-" + dayOfMonth + "-" + year
            },
            year,
            month,
            day
        )

        fromDate.setOnClickListener {
            dpd.show()
            btnGraph.visibility = View.VISIBLE
        }
        toDate.setOnClickListener {
            dpd2.show()
            btnGraph.visibility = View.VISIBLE
        }


        btnGraph.setOnClickListener {
            // apicall
            getArray(fromDateString, toDateString)
        }


    }

    private fun getArray(from: String, to: String) {
        var monitorapp = MonitorApplication.getInstance()
        var from2 =""
        val call = monitorapp.apiHelper.buildAPIServiceWithNewToken(
            monitorapp.authToken
        ).getMeasurementsForSensorWithRange(
            monitorapp.hartslagSensor!!.sensorID
        ,fromDateString, toDateString
        )
        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    var responsebody = response.body()!!


                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        // registerPatientShowErrorMessage(errorMessage)
                    } else {
                        //registerPatientShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                // registerPatientShowErrorMessage(t.message!!)
            }
        })
    }


    fun create_Graph() {

        graph.setTitle("hartslag")
        val series = LineGraphSeries(
            arrayOf(
                DataPoint(0.0, 1.0),
                DataPoint(1.0, 5.0),
                DataPoint(2.00, 3.0),
                DataPoint(3.0, 35.0),
                DataPoint(4.0, 100.0),
                DataPoint(4.5, 150.0),
                DataPoint(5.0, 65.0)
            )
        )

        val view = graph.getViewport()
        view.borderColor = ContextCompat.getColor(this.context!!, R.color.colorPrimaryDark)
        view.backgroundColor = ContextCompat.getColor(this.context!!, R.color.white)
        view.isScrollable = true
        graph.addSeries(series)
    }
}
