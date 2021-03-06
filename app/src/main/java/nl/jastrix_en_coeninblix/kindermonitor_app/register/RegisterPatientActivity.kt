package nl.jastrix_en_coeninblix.kindermonitor_app.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_register_patient.*
import nl.jastrix_en_coeninblix.kindermonitor_app.*
import nl.jastrix_en_coeninblix.kindermonitor_app.FirebaseNotifications.MyFirebaseMessagingService
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
import java.util.*
import kotlin.collections.ArrayList

class RegisterPatientActivity : BaseActivityClass() {

//    companion object {
//        var cameFromAccountFragment = false
//    }

    var noCallInProgress: Boolean = true
    lateinit var registerPatientButton: Button
    lateinit var patientFirstNameEditText: EditText
    lateinit var patientLastNameEditText: EditText
    lateinit var patientBirthDateEditText: EditText
    lateinit var patientRegisterErrorField: TextView
    var errorlist = ArrayList<String>()
    lateinit var userData: UserData

    lateinit var createdPatient: PatientWithID

    var sensorsCreatedIndex = 0
    var emailContent1: EmailContent? = null
    var emailContent2: EmailContent? = null
    var emailContent3: EmailContent? = null
    var emailContent4: EmailContent? = null

    var birthdateDay: String? = null
    var birthdateMonth: String? = null

    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)
        this.setTitle(R.string.register_patient)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        patientFirstNameEditText = findViewById(R.id.patientFirstName)
        patientLastNameEditText = findViewById(R.id.patientLastName)
        patientBirthDateEditText = findViewById(R.id.patientBirthDate)
        patientRegisterErrorField = findViewById((R.id.patientRegisterError))

        registerPatientButton = findViewById(R.id.patientRegisterButton)
        registerPatientButton.isClickable = false
        progressBar = findViewById(R.id.progressBar)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val monthPlusOne = monthOfYear + 1
                birthdateDay = dayOfMonth.toString()
                birthdateMonth = monthPlusOne.toString()
                patientBirthDateEditText.setText("" + dayOfMonth + "-" + monthPlusOne + "-" + year)
            },
            year,
            month,
            day
        )

        val datePicker = dpd.datePicker

        val currentDate = Calendar.getInstance().getTime()

        datePicker.maxDate = currentDate.time

        patientBirthDateEditText.setOnClickListener() {
            it.background = getDrawable(R.drawable.borderinput)
            dpd.show()
        }
        patientLastNameEditText.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        patientFirstNameEditText.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }


        registerPatientButton.setOnClickListener() {

            if (allFieldsFilledIn()) {
                if (noCallInProgress) {
                    progressBar.visibility = View.VISIBLE
                    patientRegisterErrorField.visibility = View.INVISIBLE
                    registerPatientButton.setBackground(getDrawable(R.drawable.round_shape_dark))

                    val patientBirthdayString = birthdateMonth + "-" + birthdateDay + "-" + year

                    val createPatient = Patient(
                        patientFirstNameEditText.text.toString(),
                        patientLastNameEditText.text.toString(),
                        patientBirthdayString
                    )

                    createPatientForThisUser(createPatient)
                }
            }
        }

        getUserData()
    }

    private fun getUserData() {
        progressBar.visibility = View.VISIBLE
        val call = MonitorApplication.getInstance().apiHelper.buildAPIServiceWithNewToken(
            MonitorApplication.getInstance().authToken
        ).getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                progressBar.visibility = View.INVISIBLE
                if (response.isSuccessful && response.body() != null) {
                    userData = response.body()!!

                    registerPatientButton.isClickable = true

                } else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        var errorMessage = "API down"
                        try {

                            val jObjError = JSONObject(response.errorBody()!!.string())
                            errorMessage = jObjError.getString("error")
                        } finally {

                        }
                        registerPatientShowErrorMessage(errorMessage)
                    } else {
                        registerPatientShowErrorMessage(response.message())
                    }
                    registerPatientButton.setBackground(getDrawable(R.drawable.rounded_shape))
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
                 registerPatientButton.setBackground(getDrawable(R.drawable.rounded_shape))
            }
        })
    }

    private fun registerPatientShowErrorMessage(errorMessage: String) {
        noCallInProgress = true
        patientRegisterErrorField.text = errorMessage
        patientRegisterErrorField.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
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
                    createdPatient = response.body()!!

                    Log.d("f", "Save the thresholds (grenswaarden) to local here")

                    createSensorForPatient(SensorType.Temperature.toString(), "35", "41")
                    createSensorForPatient(SensorType.Hartslag.toString(), "50", "200")
                    createSensorForPatient(SensorType.Adem.toString(), "10", "80")
                    createSensorForPatient(SensorType.Saturatie.toString(), "85", "100", true)

                } else {
                    if (response.code() == 500) {
                        registerPatientShowErrorMessage(getString(R.string.incorrectDateFormatError))
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
            }

            override fun onFailure(call: Call<PatientWithID>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
                registerPatientButton.setBackground(getDrawable(R.drawable.rounded_shape))
            }
        })
    }

    private fun createSensorForPatient(
        type: String,
        thresholdMin: String,
        thresholdMax: String,
        lastSensor: Boolean = false
    ) {
        val patientListIntent = Intent(this, PatientList::class.java)

        val fireBaseMessagingService = MyFirebaseMessagingService()
        val newSensor = SensorToCreate(
            type,
            "Nee",
            thresholdMin,
            thresholdMax,
            fireBaseMessagingService.getFirebaseToken(this)
        ) //Array<String>(1)
//        { fireBaseMessagingService.getFirebaseToken(this) } )

        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .createSensor(createdPatient.patientID, newSensor)

        call.enqueue(object : Callback<Sensor> {
            override fun onResponse(
                call: Call<Sensor>,
                response: Response<Sensor>
            ) {
                if (lastSensor) {
                    progressBar.visibility = View.INVISIBLE
                }

                if (response.isSuccessful && response.body() != null) {
                    noCallInProgress = true

                    val callbackResponse = response.body()!!

                    when (callbackResponse.type) {
                        SensorType.Temperature.toString() ->
                            emailContent1 = EmailContent(
                                callbackResponse.sensorID.toString(),
                                callbackResponse.preSharedKey,
                                callbackResponse.type
                            )
                        SensorType.Hartslag.toString() ->
                            emailContent2 = EmailContent(
                                callbackResponse.sensorID.toString(),
                                callbackResponse.preSharedKey,
                                callbackResponse.type
                            )
                        SensorType.Adem.toString() ->
                            emailContent3 = EmailContent(
                                callbackResponse.sensorID.toString(),
                                callbackResponse.preSharedKey,
                                callbackResponse.type
                            )
                        SensorType.Saturatie.toString() ->
                            emailContent4 = EmailContent(
                                callbackResponse.sensorID.toString(),
                                callbackResponse.preSharedKey,
                                callbackResponse.type
                            )
                    }

                    sensorsCreatedIndex++
                    if (sensorsCreatedIndex == 4) {
                        sensorsCreatedIndex = 0
                        val patientFullName =
                            patientFirstName.text.toString() + " " + patientLastName.text.toString()

                        var emailContents = ArrayList<EmailContent>()

                        if (emailContent1 != null) {
                            emailContents.add(emailContent1!!)
                        }
                        if (emailContent2 != null) {
                            emailContents.add(emailContent2!!)
                        }
                        if (emailContent3 != null) {
                            emailContents.add(emailContent3!!)
                        }
                        if (emailContent4 != null) {
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
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            registerPatientShowErrorMessage(errorMessage)
                        } catch (e: Exception) {

                            registerPatientShowErrorMessage(response.message())
                        }
                    } else {
                        registerPatientShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Sensor>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
                progressBar.visibility = View.INVISIBLE
                registerPatientButton.setBackground(getDrawable(R.drawable.rounded_shape))
            }
        })
    }

    override fun onBackPressed() {
//        val ss: String = intent.getBooleanExtra("cameFromAccountFragment")
        if (intent.getBooleanExtra("cameFromAccountFragment", false)) {
            val mainActivity = Intent(this, MainActivity::class.java)
            mainActivity.putExtra("openAccountFragment", true)
            startActivity(mainActivity)
        } else {
            val patientListIntent = Intent(this, PatientList::class.java)
            startActivity(patientListIntent)
        }
        finish()
    }

    private fun allFieldsFilledIn(): Boolean {
        errorlist.clear()
        if (isNullOrEmpty(patientBirthDateEditText.text.toString())) {
            val string = "Geboortedatum"
            patientBirthDateEditText.setBackgroundColor(getColor(R.color.colorBad))
            errorlist.add(string)
        }
        if (isNullOrEmpty(patientFirstNameEditText.text.toString())) {
            val string = "voornaam"
            patientFirstNameEditText.setBackgroundColor(getColor(R.color.colorBad))
            errorlist.add(string)
        }
        if (isNullOrEmpty(patientLastNameEditText.text.toString())) {
            val string = "achternaam"
            patientLastNameEditText.setBackgroundColor(getColor(R.color.colorBad))
            errorlist.add(string)
        }

        if (errorlist.count() > 0) {
            var string = ""
            val max = errorlist.count()
            var index = 0
            errorlist.forEach {
                if (index < max - 1) {
                    string = string + it + ", "
                } else {
                    string = string + it + " moet(en) worden ingevuld"
                }
                index++
            }
            patientRegisterErrorField.text = string
            patientRegisterErrorField.visibility = View.VISIBLE
            return false
        } else {
            return true
        }

    }

    private fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.isEmpty()) {
            return false
        } else {
            return true
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
