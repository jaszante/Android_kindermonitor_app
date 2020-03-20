package nl.jastrix_en_coeninblix.kindermonitor_app.ui.gallery

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Measurement
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var fromDate: EditText
    private lateinit var toDate: EditText
    private lateinit var btnGraph: Button
    private lateinit var graph: LineChart
    private lateinit var dropdown: Spinner
    var fromDateString: String = ""
    var toDateString: String = ""
    var type_selected: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var errorField: TextView

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
        progressBar = currentView.findViewById(R.id.progressBar)
        graph = currentView.findViewById(R.id.graph)
        btnGraph = currentView.findViewById(R.id.btn_Graph)
        errorField = currentView.findViewById(R.id.errorField)

        fromDate = currentView.findViewById(R.id.fromDate)
        toDate = currentView.findViewById(R.id.toDate)
        dropdown = currentView.findViewById(R.id.dropdown_type)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month2 = month + 1
        val day2 = day + 1

        fromDateString = "" + year + "-" + month2 + "-" + day + "T" + "00:00:00.000"
        toDateString = "" + year + "-" + month2 + "-" + day2 + "T" + "00:00:00.000"
        val dpd = DatePickerDialog(
            context!!,
            DatePickerDialog.OnDateSetListener { datePickerView, datePickerYear, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                fromDate.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + datePickerYear)
                fromDateString =
                    "" + datePickerYear + "-" + monthPlusOne + "-" + dayOfMonth + "T" + "00:00:00.000"
            },
            year,
            month,
            day
        )
        val dpd2 = DatePickerDialog(
            context!!,
            DatePickerDialog.OnDateSetListener { datePickerView, datePickerYear, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                toDate.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + datePickerYear)
                toDateString =
                    "" + datePickerYear + "-" + monthPlusOne + "-" + dayOfMonth + "T" + "23:59:59.000"
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
            type_selected = dropdown.selectedItem.toString()
            getArray(type_selected)

        }
        val array: Array<String> =
            arrayOf("Hartslag", "Saturatie", "Adem Frequentie", "Temperatuur")

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = adapter
        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                dropdown.setBackgroundColor(resources.getColor(R.color.colorBad))
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                type_selected = adapter.getItem(position)!!
            }
        }
    }

    private fun getArray(type: String) {
        progressBar.visibility = View.VISIBLE
        errorField.visibility = View.INVISIBLE
        val monitorapp = MonitorApplication.getInstance()
        var id = 0
        when (type) {
            "Hartslag" -> id = monitorapp.hartslagSensor!!.sensorID
            "Saturatie" -> id = monitorapp.saturatieSensor!!.sensorID
            "Temperatuur" -> id = monitorapp.temperatuurSensor!!.sensorID
            "Adem Frequentie" -> id = monitorapp.ademFrequentieSensor!!.sensorID
            else -> Log.d("TypeERror", "unsupported sensor")
        }

        val call = monitorapp.apiHelper.buildAPIServiceWithNewToken(
            monitorapp.authToken
        ).getMeasurementsForSensorWithRange(
            id, fromDateString, toDateString
        )

        call.enqueue(object : Callback<Array<Measurement>> {
            override fun onResponse(
                call: Call<Array<Measurement>>,
                response: Response<Array<Measurement>>
            ) {
                progressBar.visibility = View.INVISIBLE
                if (response.isSuccessful && response.body() != null) {
                    val responsebody = response.body()!!
                    val list: Array<Measurement> = responsebody
                    create_Graph(list)

                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        showGraphRequestErrorMessage(errorMessage)
                    } else {
                        showGraphRequestErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Array<Measurement>>, t: Throwable) {
                showGraphRequestErrorMessage(t.message!!)
            }

            private fun showGraphRequestErrorMessage(message: String) {
                errorField.visibility = View.VISIBLE
                errorField.text = message
            }
        })
    }

    fun create_Graph(array: Array<Measurement>) {
        val list = ArrayList<Entry>()
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.GERMANY)

        array.forEach {
            val date = format.parse(it.time)
            val point = Entry(date.time.toFloat(), it.value.toFloat())
            list.add(point)
        }
        //  var arrayDP = arrayOfNulls<DataPoint>(list.size)
        //  var de = list.toArray(arrayDP)


        /*   var entries = ArrayList<Entry>()
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
           entriesLaag.add(Entry9)*/

        val dataSet = LineDataSet(list, "lijn gemiddelt");
        // var dataSet2 = LineDataSet(entriesHoog, "lijn hoog");
        //  var dataSet3 = LineDataSet(entriesLaag, "lijn laag");
        /*    dataSet.setColor(R.color.colorPrimary)
            dataSet2.setColor(R.color.colorBad)
            dataSet3.setColor(R.color.colorGood)*/
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)
        //  dataSets.add(dataSet2)
        // dataSets.add(dataSet3)
        val lineData = LineData(dataSets)
        graph.data = lineData
        graph.invalidate()
    }
}
