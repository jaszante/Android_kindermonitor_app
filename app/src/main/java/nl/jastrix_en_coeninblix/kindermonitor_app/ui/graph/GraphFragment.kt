package nl.jastrix_en_coeninblix.kindermonitor_app.ui.graph

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
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
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GraphFragment : Fragment() {

    private lateinit var graphViewModel: GraphViewModel
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
    private lateinit var currentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        graphViewModel =
            ViewModelProviders.of(this).get(GraphViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_graph, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentView = getView()!!
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
            btnGraph.setBackground(getDrawable(currentView.context, R.drawable.round_shape_dark))
            type_selected = dropdown.selectedItem.toString()
            getArray(type_selected)

        }
        val array: Array<String> =
            arrayOf("Hartslag", "Saturatie", "Adem Frequentie", "Temperatuur")

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, array)
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
            else -> Log.d("TypeError", "unsupported sensor")
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
                btnGraph.setBackground(
                    getDrawable(
                        currentView.context,
                        R.drawable.rounded_shape
                    )
                )
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
                btnGraph.setBackground(
                    getDrawable(
                        currentView.context,
                        R.drawable.rounded_shape
                    )
                )
            }

            private fun showGraphRequestErrorMessage(message: String) {
                errorField.visibility = View.VISIBLE
                errorField.text = message
            }
        })
    }

    fun create_Graph(allMeasurements: Array<Measurement>) {
        val firstChosenDay = toDateString.split('T')
        val secondChosenDay = fromDateString.split('T')

        val showOneDay = firstChosenDay[0] == secondChosenDay[0]
//        val showOneDay = abs(firstChosenDay[0].split('-')[2].toInt() - secondChosenDay[0].split('-')[2].toInt()) < 2

        val averageGraphPoints = ArrayList<Entry>()
        val highestGraphPoints = ArrayList<Entry>()
        val lowestGraphPoints = ArrayList<Entry>()

        var highestPoint: Double = allMeasurements[0].value
        var lowestPoint: Double = allMeasurements[0].value
        var averagePoint: Double = 0.0
        val allMeasurementsWithinAverage = ArrayList<Double>()
        var previousTimeForGraph: Float = -1f

        if (showOneDay) {
            var index = 1
            var calculatingAverageForThisHour: Int =
                returnFormattedDate(allMeasurements[0].time).hours
            allMeasurements.forEach {
                val date = returnFormattedDate(it.time)
                if (date.hours == calculatingAverageForThisHour) {
                    allMeasurementsWithinAverage.add(it.value)

                    if (it.value > highestPoint) {
                        highestPoint = it.value
                    }
                    if (it.value < lowestPoint) {
                        lowestPoint = it.value
                    }

                    if (index == allMeasurements.count()) {
                        averagePoint = 0.0
                        allMeasurementsWithinAverage.forEach {
                            averagePoint += it
                        }
                        averagePoint /= allMeasurementsWithinAverage.count()
                        allMeasurementsWithinAverage.clear()
                        calculatingAverageForThisHour = date.hours
                        allMeasurementsWithinAverage.add(it.value)

                        val averagePointPoint = Entry(previousTimeForGraph, averagePoint.toFloat())
                        averageGraphPoints.add(averagePointPoint)

                        val highestPointPoint = Entry(previousTimeForGraph, highestPoint.toFloat())
                        highestGraphPoints.add(highestPointPoint)

                        val lowestPointPoint = Entry(previousTimeForGraph, lowestPoint.toFloat())
                        lowestGraphPoints.add(lowestPointPoint)
                    }
                } else {
                    averagePoint = 0.0
                    allMeasurementsWithinAverage.forEach {
                        averagePoint += it
                    }
                    averagePoint /= allMeasurementsWithinAverage.count()
                    allMeasurementsWithinAverage.clear()
                    calculatingAverageForThisHour = date.hours
                    allMeasurementsWithinAverage.add(it.value)

                    val averagePointPoint = Entry(previousTimeForGraph, averagePoint.toFloat())
                    averageGraphPoints.add(averagePointPoint)

                    val highestPointPoint = Entry(previousTimeForGraph, highestPoint.toFloat())
                    highestGraphPoints.add(highestPointPoint)
                    highestPoint = it.value

                    val lowestPointPoint = Entry(previousTimeForGraph, lowestPoint.toFloat())
                    lowestGraphPoints.add(lowestPointPoint)
                    lowestPoint = it.value
                }

                previousTimeForGraph = date.hours.toFloat()
                index++
            }
        } else {
            var calculatingAverageForThisDay: Int =
                allMeasurements[0].time.split('-')[2].split('T')[0].toInt() //returnFormattedDate(allMeasurements[0].time).day // allMeasurements[0].time.split('-')[2].split('T')[0].toInt()
            var index = 1
            allMeasurements.forEach {
                //                val date = returnFormattedDate(it.time)
                if (it.time.split('-')[2].split('T')[0].toInt() == calculatingAverageForThisDay) {
                    allMeasurementsWithinAverage.add(it.value)

                    if (it.value > highestPoint) {
                        highestPoint = it.value
                    }
                    if (it.value < lowestPoint) {
                        lowestPoint = it.value
                    }

                    if (index == allMeasurements.count()) {
                        averagePoint = 0.0
                        allMeasurementsWithinAverage.forEach {
                            averagePoint += it
                        }
                        averagePoint /= allMeasurementsWithinAverage.count()
                        allMeasurementsWithinAverage.clear()
                        calculatingAverageForThisDay = it.time.split('-')[2].split('T')[0].toInt()
                        allMeasurementsWithinAverage.add(it.value)

                        val averagePointPoint = Entry(previousTimeForGraph, averagePoint.toFloat())
                        averageGraphPoints.add(averagePointPoint)

                        val highestPointPoint = Entry(previousTimeForGraph, highestPoint.toFloat())
                        highestGraphPoints.add(highestPointPoint)

                        val lowestPointPoint = Entry(previousTimeForGraph, lowestPoint.toFloat())
                        lowestGraphPoints.add(lowestPointPoint)
                    }
                } else {
                    averagePoint = 0.0
                    allMeasurementsWithinAverage.forEach {
                        averagePoint += it
                    }
                    averagePoint /= allMeasurementsWithinAverage.count()
                    allMeasurementsWithinAverage.clear()
                    calculatingAverageForThisDay = it.time.split('-')[2].split('T')[0].toInt()
                    allMeasurementsWithinAverage.add(it.value)

                    val averagePointPoint = Entry(previousTimeForGraph, averagePoint.toFloat())
                    averageGraphPoints.add(averagePointPoint)

                    val highestPointPoint = Entry(previousTimeForGraph, highestPoint.toFloat())
                    highestGraphPoints.add(highestPointPoint)
                    highestPoint = it.value

                    val lowestPointPoint = Entry(previousTimeForGraph, lowestPoint.toFloat())
                    lowestGraphPoints.add(lowestPointPoint)
                    lowestPoint = it.value
                }

                previousTimeForGraph = it.time.split('-')[2].split('T')[0].toInt().toFloat()
                index++
            }

        }

        val dataSet = LineDataSet(averageGraphPoints, "Gemiddelde");
        val dataSet2 = LineDataSet(highestGraphPoints, "Hoogste punt");
        val dataSet3 = LineDataSet(lowestGraphPoints, "Laagste punt");
        val colorB = getColor(context!!, R.color.black)
        val colorP = getColor(context!!, R.color.colorPrimary)
        val colorPD = getColor(context!!, R.color.colorPrimaryDark)
        dataSet.setColor(colorB)
        dataSet2.setColor(colorP)
        dataSet3.setColor(colorPD)
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)
        dataSets.add(dataSet2)
        dataSets.add(dataSet3)
        val lineData = LineData(dataSets)
        graph.xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        graph.description.text = dropdown.selectedItem.toString()
        graph.data = lineData
        graph.invalidate()
    }

    private fun returnFormattedDate(time: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.GERMANY)
        val formatNoMillisecond = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY)

        var date: Date
        try {
            date = formatNoMillisecond.parse(time)
        } catch (e: Exception) {
            date = format.parse(time)
        }
        return date
    }
}
