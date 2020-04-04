package nl.jastrix_en_coeninblix.kindermonitor_app.ui.slideshow


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Sensor
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.SensorToCreate
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var hartslagMin: TextView
    private lateinit var hartslagMax: TextView

    private lateinit var ademfrequentieMin: TextView
    private lateinit var ademfrequentieMax: TextView

    private lateinit var saturatieMin: TextView
    private lateinit var saturatieMax: TextView

    private lateinit var temperatuurMin: TextView
    private lateinit var temperatuurMax: TextView

    private var changed: Boolean = false
    private lateinit var buttonSave: Button
    private lateinit var spinner: Spinner

    private var noCallInProgress: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentView = getView()!!
        hartslagMin = currentView.findViewById(R.id.hartslagMin)
        hartslagMin.setOnClickListener {
            open_Number_picker(hartslagMin, 50, 200)

        }
        hartslagMax = currentView.findViewById(R.id.hartslagMax)
        hartslagMax.setOnClickListener {
            open_Number_picker(hartslagMax, 50, 200)

        }

        ademfrequentieMin = currentView.findViewById(R.id.ademfrequentieMin)
        ademfrequentieMin.setOnClickListener {
            open_Number_picker(ademfrequentieMin, 10, 80)

        }
        ademfrequentieMax = currentView.findViewById(R.id.ademfrequentieMax)
        ademfrequentieMax.setOnClickListener {
            open_Number_picker(ademfrequentieMax, 10, 80)

        }

        saturatieMin = currentView.findViewById(R.id.saturatieMin)
        saturatieMin.setOnClickListener {
            open_Number_picker(saturatieMin, 85, 100)

        }
        saturatieMax = currentView.findViewById(R.id.saturatieMax)
        saturatieMax.setOnClickListener {
            open_Number_picker(saturatieMax, 85, 100)
        }

        temperatuurMin = currentView.findViewById(R.id.temperatuurMin)
        temperatuurMin.setOnClickListener {
            open_Number_picker(temperatuurMin, 35, 41)

        }
        temperatuurMax = currentView.findViewById(R.id.temperatuurMax)
        temperatuurMax.setOnClickListener {
            open_Number_picker(temperatuurMax, 35, 41)
        }

        buttonSave = currentView.findViewById(R.id.buttonSave)
        buttonSave.setOnClickListener {
            val monitorApplication = MonitorApplication.getInstance()

            monitorApplication.hartslagThresholds.postValue(hartslagMin.text.toString() + " - " + hartslagMax.text.toString())
            monitorApplication.temperatuurThresholds.postValue(temperatuurMin.text.toString() + "° - " + temperatuurMax.text.toString() + "°")
            monitorApplication.saturatieThresholds.postValue(saturatieMin.text.toString() + "% - " + saturatieMax.text.toString() + "%")
            monitorApplication.ademfrequentieThresholds.postValue(ademfrequentieMin.text.toString() + " - " + ademfrequentieMax.text.toString())

            monitorApplication.hartslagSensor!!.thresholdMin = hartslagMin.text.toString().toInt()
            monitorApplication.hartslagSensor!!.thresholdMax = hartslagMax.text.toString().toInt()

            monitorApplication.temperatuurSensor!!.thresholdMin = temperatuurMin.text.toString().toInt()
            monitorApplication.temperatuurSensor!!.thresholdMax = temperatuurMax.text.toString().toInt()

            monitorApplication.ademFrequentieSensor!!.thresholdMin = ademfrequentieMin.text.toString().toInt()
            monitorApplication.ademFrequentieSensor!!.thresholdMax = ademfrequentieMax.text.toString().toInt()

            monitorApplication.saturatieSensor!!.thresholdMin = saturatieMin.text.toString().toInt()
            monitorApplication.saturatieSensor!!.thresholdMax = saturatieMax.text.toString().toInt()

            if (noCallInProgress) {
                noCallInProgress= false

                val updatedHartslagSensor = SensorToCreate(
                    monitorApplication.hartslagSensor!!.sensorType.toString(),
                    "Nee",
                    hartslagMin.toString(),
                    hartslagMax.toString(),
                    monitorApplication.hartslagSensor!!.PushnotificationDeviceToken
                )
                updateSensorThresholds(updatedHartslagSensor)

                val updatedTemperatuurSensor = SensorToCreate(
                    monitorApplication.temperatuurSensor!!.sensorType.toString(),
                    "Nee",
                    temperatuurMin.toString(),
                    temperatuurMax.toString(),
                    monitorApplication.temperatuurSensor!!.PushnotificationDeviceToken
                )
                updateSensorThresholds(updatedTemperatuurSensor)

                val updatedSaturatieSensor = SensorToCreate(
                    monitorApplication.saturatieSensor!!.sensorType.toString(),
                    "Nee",
                    saturatieMin.toString(),
                    saturatieMax.toString(),
                    monitorApplication.saturatieSensor!!.PushnotificationDeviceToken
                )
                updateSensorThresholds(updatedSaturatieSensor)

                val updatedAdemFrequentieSensor = SensorToCreate(
                    monitorApplication.ademFrequentieSensor!!.sensorType.toString(),
                    "Nee",
                    ademfrequentieMin.toString(),
                    ademfrequentieMax.toString(),
                    monitorApplication.ademFrequentieSensor!!.PushnotificationDeviceToken
                )
                updateSensorThresholds(updatedAdemFrequentieSensor)
            }

            buttonSave.visibility = View.INVISIBLE
        }
        spinner = currentView.findViewById(R.id.spinner_alarm)
        val array: Array<String> = arrayOf("30 sec", "60 sec", "90 sec", "120 sec ", "180 sec")

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = adapter.getItem(position)
                val newValue = item.split(" sec")[0] + "000"
                MonitorApplication.getInstance().pauzeTime = newValue.toLong()
            }
        }
    }

    private fun updateSensorThresholds(updatedHartslagSensor: SensorToCreate){
        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .updateSensor(MonitorApplication.getInstance().hartslagSensor!!.sensorID, updatedHartslagSensor)

        call.enqueue(object : Callback<Sensor> {
            override fun onResponse(
                call: Call<Sensor>,
                response: Response<Sensor>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    noCallInProgress = true
                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            updateSensorShowErrorMessage(errorMessage)
                        }
                        catch (e: Exception) {
                            updateSensorShowErrorMessage(response.message())
                        }
                    } else {
                        updateSensorShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Sensor>, t: Throwable) {
                updateSensorShowErrorMessage(t.message!!)
            }
        })
    }

    private fun updateSensorShowErrorMessage(message: String){

    }

    private val changeAdemFrequentieThresholds = Observer<String> {
            value ->
        value?.let {
            val minAndMax = it.split(" - ")
            ademfrequentieMin.text = minAndMax[0]
            ademfrequentieMax.text = minAndMax[1]
        }
    }

    private val changeHartslagThresholds = Observer<String> {
            value ->
        value?.let {
            val minAndMax = it.split(" - ")
            hartslagMin.text = minAndMax[0]
            hartslagMax.text = minAndMax[1]
        }
    }

    private val changeTemperatuurThresholds = Observer<String> {
            value ->
        value?.let {
            val minAndMax = it.split(" - ")
            val min = minAndMax[0].replace("°", "")
            val max = minAndMax[1].replace("°", "")
            temperatuurMin.text = min
            temperatuurMax.text = max
        }
    }

    private val changeSaturatieThresholds = Observer<String> {
            value ->
        value?.let {
            val minAndMax = it.split(" - ")
            val min = minAndMax[0].replace("%", "")
            val max = minAndMax[1].replace("%", "")
            saturatieMin.text = min
            saturatieMax.text = max
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val monitorApplication = MonitorApplication.getInstance()

        monitorApplication.hartslagThresholds.observe(this, changeHartslagThresholds)
        monitorApplication.temperatuurThresholds.observe(this, changeTemperatuurThresholds)
        monitorApplication.saturatieThresholds.observe(this, changeSaturatieThresholds)
        monitorApplication.ademfrequentieThresholds.observe(this, changeAdemFrequentieThresholds)
    }


    fun open_Number_picker(textview: TextView, min: Int, max: Int) {

        val linearLayout = layoutInflater.inflate(R.layout.view_number_picker, null) as LinearLayout
        val numberpicker = linearLayout.findViewById<View>(R.id.numberPicker1) as NumberPicker
        numberpicker.minValue = min
        numberpicker.maxValue = max

        val builder: AlertDialog = AlertDialog.Builder(this.context)
            .setPositiveButton("Selecteer", null)
            .setNegativeButton("Annuleren", null)
            .setView(linearLayout)
            .setCancelable(false)
            .create()
        builder.show()
        //Setting up OnClickListener on positive button of AlertDialog
        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE)
            .setOnClickListener(View.OnClickListener {
                textview.text = numberpicker.value.toString()
                builder.cancel()
                changed = true
                buttonSave.visibility = View.VISIBLE

            })
    }
}