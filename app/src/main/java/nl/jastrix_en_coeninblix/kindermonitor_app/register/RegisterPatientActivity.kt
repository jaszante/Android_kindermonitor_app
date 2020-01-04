package nl.jastrix_en_coeninblix.kindermonitor_app.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register_patient.*
import nl.jastrix_en_coeninblix.kindermonitor_app.*
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*
import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.security.Timestamp
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class RegisterPatientActivity : BaseActivityClass() {

    var noCallInProgress: Boolean = true
    lateinit var registerPatientButton: Button
    lateinit var patientFirstNameEditText: EditText
    lateinit var patientLastNameEditText: EditText
    lateinit var patientBirthDateEditText: EditText
    lateinit var patientRegisterErrorField: TextView

    lateinit var userData: UserData

    lateinit var createdPatient: PatientWithID

    var sensorsCreatedIndex = 0
    var emailContent1: EmailContent? = null
    var emailContent2: EmailContent? = null
    var emailContent3: EmailContent? = null
    var emailContent4: EmailContent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)

        // after startactivity do finish()
        patientFirstNameEditText = findViewById(R.id.patientFirstName)
        patientLastNameEditText = findViewById(R.id.patientLastName)
        patientBirthDateEditText = findViewById(R.id.patientBirthDate)
        patientRegisterErrorField = findViewById((R.id.patientRegisterError))

        registerPatientButton = findViewById(R.id.patientRegisterButton)
        registerPatientButton.isClickable = false


        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        patientBirthDateEditText.setOnClickListener() {
            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val monthPlusOne = monthOfYear + 1
                    patientBirthDateEditText.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + year)
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        registerPatientButton.setOnClickListener() {

            patientRegisterErrorField.visibility = View.INVISIBLE

            val patientBirthdayString = patientBirthDateEditText.text.toString()
//            var parsedDateString: String = ""
//            var parseSucceeded: Boolean = false
//
//            try {
////                val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
////                parsedDateString = getDateInstance("DD-MM-yyyy").parse(patientBirthdayString) // SimpleDateFormat("dd-MM-yyyy").parse(patientBirthdayString) //getDateInstance(patientBirthdayString)
////                parsedDateString = format.parse(patientBirthdayString)
//
////                val parser =  SimpleDateFormat("dd.mm.yyyy") //SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
////                val formatter = SimpleDateFormat("dd.mm.yyyy");
////                parsedDateString = formatter.format(parser.parse(patientBirthdayString));
//
//                parseSucceeded = true
//            } catch (pe: Exception) {
//                registerPatientShowErrorMessage(getString(R.string.incorrectDateFormatError))
//            }

//            if (parseSucceeded) {
                val createPatient = Patient(
                    patientFirstNameEditText.text.toString(),
                    patientLastNameEditText.text.toString(),
                    patientBirthdayString
//                    parsedDateString.toString()
                )

                createPatientForThisUser(createPatient)
//            }
        }

        getUserData()
    }

    private fun getUserData() {
        val call = MonitorApplication.getInstance().apiHelper.buildAPIServiceWithNewToken(
            MonitorApplication.getInstance().authToken
        ).getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                if (response.isSuccessful && response.body() != null) {
                    userData = response.body()!!

                    registerPatientButton.isClickable = true

                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        registerPatientShowErrorMessage(errorMessage)
                    } else {
                        registerPatientShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
            }
        })
    }

    private fun registerPatientShowErrorMessage(errorMessage: String) {
        noCallInProgress = true
        patientRegisterErrorField.text = errorMessage
        patientRegisterErrorField.visibility = View.VISIBLE
    }

    private fun createPatientForThisUser(patient: Patient) {
        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .createPatientForLoggedInUser(patient)

        call.enqueue(object : Callback<PatientWithID> {
            override fun onResponse(
                call: Call<PatientWithID>,
                response: Response<PatientWithID>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    noCallInProgress = true
                    createdPatient = response.body()!!

                    Log.d("f", "Save the thresholds (grenswaarden) to local here")

                    createSensorForPatient(SensorType.Temperature.toString(), "35", "41")
                    createSensorForPatient(SensorType.Hartslag.toString(), "50", "200")
                    createSensorForPatient(SensorType.Adem.toString(), "10", "80")
                    createSensorForPatient(SensorType.Saturatie.toString(), "85", "100")

                } else {
                    if (response.code() == 500){
                        registerPatientShowErrorMessage(getString(R.string.incorrectDateFormatError))
                    }
                    else{
                        val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                        if (errorbodyLength != 0) {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            registerPatientShowErrorMessage(errorMessage)
                        } else {
                            registerPatientShowErrorMessage(response.message())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PatientWithID>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
            }
        })
    }

    private fun createSensorForPatient(type: String, thresholdMin: String, thresholdMax: String) {
        val patientListIntent = Intent(this, PatientList::class.java)

        val newSensor = SensorToCreate(type, "Nee", thresholdMin, thresholdMax)
        var call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .createSensor(createdPatient.patientID, newSensor)

        call.enqueue(object : Callback<Sensor> {
            override fun onResponse(
                call: Call<Sensor>,
                response: Response<Sensor>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    noCallInProgress = true

                    val callbackResponse = response.body()!!

                    when (callbackResponse.type){
                        SensorType.Temperature.toString() ->
                            emailContent1 = EmailContent(callbackResponse.sensorID.toString(), callbackResponse.preSharedKey, callbackResponse.type)
                        SensorType.Hartslag.toString() ->
                            emailContent2 = EmailContent(callbackResponse.sensorID.toString(), callbackResponse.preSharedKey, callbackResponse.type)
                        SensorType.Adem.toString() ->
                            emailContent3 = EmailContent(callbackResponse.sensorID.toString(), callbackResponse.preSharedKey, callbackResponse.type)
                        SensorType.Saturatie.toString() ->
                            emailContent4 = EmailContent(callbackResponse.sensorID.toString(), callbackResponse.preSharedKey, callbackResponse.type)
                    }

                    sensorsCreatedIndex++
                    if (sensorsCreatedIndex == 4){
                        sensorsCreatedIndex = 0
                        val patientFullName = patientFirstName.text.toString() + " " + patientLastName.text.toString()

                        var emailContents = ArrayList<EmailContent>()

                        if (emailContent1 != null){
                            emailContents.add(emailContent1!!)
                        }
                        if (emailContent2 != null){
                            emailContents.add(emailContent2!!)
                        }
                        if (emailContent3 != null){
                            emailContents.add(emailContent3!!)
                        }
                        if (emailContent4 != null){
                            emailContents.add(emailContent4!!)
                        }

                        val sendPreSharedKey = SendPreSharedKey(emailContents, patientFullName)
                        sendPreSharedKey.sendPreSharedKeyToEmail()

                        startActivity(patientListIntent)
                        finish()
                    }

                    // permission not nessesary because this user is creating the patient?
//                    setSensorPersmissionForCurrentUser()
                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        registerPatientShowErrorMessage(errorMessage)
                    } else {
                        registerPatientShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Sensor>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
            }
        })
    }

//    // permission not nessesary because this user is creating the patient?
//    private fun setSensorPersmissionForCurrentUser() {
//        val patientListIntent: Intent = Intent(this, PatientList::class.java)

//        var call = apiHelper.returnAPIServiceWithAuthenticationTokenAdded().permission(createdPatient.patientID, newSensor)
//
//        call.enqueue(object : Callback<Sensor> {
//            override fun onResponse(
//                call: Call<Sensor>,
//                response: Response<Sensor>
//            ) {
//                if (response.isSuccessful && response.body() != null) {
//                    startActivity(patientListIntent)
//                    finish()
//                }
//                else
//                {
//                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
//                    if (errorbodyLength != 0) {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        val errorMessage = jObjError.getString("error")
//                        registerPatientShowErrorMessage(errorMessage)
//                    } else {
//                        registerPatientShowErrorMessage(response.message())
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<Sensor>, t: Throwable) {
//                registerPatientShowErrorMessage(t.message!!)
//            }
//        })
//    }
}
