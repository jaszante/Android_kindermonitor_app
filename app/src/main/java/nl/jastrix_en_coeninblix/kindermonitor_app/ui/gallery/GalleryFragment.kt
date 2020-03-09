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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var fromDate: EditText
    private lateinit var toDate: EditText
    private lateinit var btnGraph: Button
    private lateinit var graph: LineChart
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
        graph = currentView.findViewById(R.id.graph)
        btnGraph = currentView.findViewById(R.id.btn_Graph)

        fromDate = currentView.findViewById(R.id.fromDate)
        toDate = currentView.findViewById(R.id.toDate)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var month2 = month + 1
        var day2 = day + 1

        fromDateString = "" + year + "-" + month2 + "-" + day + "T" + "00:00:00.000"
        toDateString = "" + year + "-" + month2 + "-" + day2 + "T" + "00:00:00.000"
        val dpd = DatePickerDialog(
            this.context,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                fromDate.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + year)
                fromDateString =
                    "" + year + "-" + monthPlusOne + "-" + dayOfMonth + "T" + "00:00:00.000"
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
                toDateString =
                    "" + year + "-" + monthPlusOne + "-" + dayOfMonth + "T" + "23:59:59.000"
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


        val call = monitorapp.apiHelper.buildAPIServiceWithNewToken(
            monitorapp.authToken
        ).getMeasurementsForSensorWithRange(
            monitorapp.hartslagSensor!!.sensorID
            , fromDateString, toDateString
        )
        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    var responsebody = response.body()!!
                    var list: Array<Measurement> = responsebody
                    create_Graph(list)

                } else {
//                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
//                    if (errorbodyLength != 0) {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        val errorMessage = jObjError.getString("error")
//                        // registerPatientShowErrorMessage(errorMessage)
//                    } else {
//                        //registerPatientShowErrorMessage(response.message())
//                    }
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                // registerPatientShowErrorMessage(t.message!!)
            }
        })
    }


    fun create_Graph(array: Array<Measurement>) {
/*
        var list = ArrayList<Entry>()
        var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.GERMANY)

        array.forEach {
            var date = format.parse(it.time)
            var point = Entry(it.time.toFloat(), it.value.toFloat())
            list.add(point)
        }
        var arrayDP = arrayOfNulls<DataPoint>(list.size)
        var de = list.toArray(arrayDP)

        var series: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>(de)*/

        var entries = ArrayList<Entry>()
        var entriesHoog = ArrayList<Entry>()
        var entriesLaag = ArrayList<Entry>()
        val Entry1 = Entry(1.toFloat(), 1.toFloat())
        val Entry2 = Entry(2.toFloat(), 2.toFloat())
        val Entry3 = Entry(3.toFloat(), 3.toFloat())
        entries.add(Entry1)
        entries.add(Entry2)
        entries.add(Entry3)
        val Entry4 = Entry(1.toFloat(), 2.toFloat())
        val Entry5 = Entry(2.toFloat(), 3.toFloat())
        val Entry6 = Entry(3.toFloat(), 4.toFloat())
        entriesHoog.add(Entry4)
        entriesHoog.add(Entry5)
        entriesHoog.add(Entry6)
        val Entry7 = Entry(1.toFloat(), 0.toFloat())
        val Entry8 = Entry(2.toFloat(), 1.toFloat())
        val Entry9 = Entry(3.toFloat(), 2.toFloat())
        entriesLaag.add(Entry7)
        entriesLaag.add(Entry8)
        entriesLaag.add(Entry9)

        var dataSet =  LineDataSet(entries, "lijn gemiddelt");
        var dataSet2 =  LineDataSet(entriesHoog, "lijn hoog");
        var dataSet3 =  LineDataSet(entriesLaag, "lijn laag");
    /*    dataSet.setColor(R.color.colorPrimary)
        dataSet2.setColor(R.color.colorBad)
        dataSet3.setColor(R.color.colorGood)*/
        var dataSets =  ArrayList<ILineDataSet>()
        dataSets.add(dataSet)
        dataSets.add(dataSet2)
        dataSets.add(dataSet3)
        val lineData = LineData(dataSets)
        graph.data = lineData
        graph.invalidate()
    }
}
