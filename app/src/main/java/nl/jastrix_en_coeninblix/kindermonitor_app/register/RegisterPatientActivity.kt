package nl.jastrix_en_coeninblix.kindermonitor_app.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Patient
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RegisterPatientActivity : AppCompatActivity() {

    var noCallInProgress: Boolean = true
    lateinit var registerPatientButton: Button
    lateinit var patientFirstNameEditText: EditText
    lateinit var patientLastNameEditText: EditText
    lateinit var patientBirthDateEditText: EditText
    lateinit var patientRegisterErrorField: TextView

    lateinit var userData: UserData

    lateinit var createdPatient: PatientWithID

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
                    // Display Selected date in TextView
                    patientBirthDateEditText.setText("" + dayOfMonth + " - " + month + " - " + year)
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        registerPatientButton.setOnClickListener() {
            val patientBirthdayString = patientBirthDateEditText.text.toString()
            var parsedDateString: Date? = Date()
            var parseSucceeded: Boolean = false

            try {
                val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
                parsedDateString = format.parse(patientBirthdayString)
                parseSucceeded = true
            } catch (pe: Exception) {
                patientRegisterErrorField.text = getString(R.string.incorrectDateFormatError)
            }

            if (parseSucceeded) {
                val createPatient = Patient(
                    patientFirstNameEditText.text.toString(),
                    patientLastNameEditText.text.toString(),
                    parsedDateString!!
                )

                createPatientForThisUser(createPatient)
            }
        }

        getUserData()
    }

    private fun getUserData(){
        val call = MainActivity.apiHelper.buildAPIServiceWithNewToken(authToken).getCurrentUser()
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
        var call = MainActivity.apiHelper.returnAPIServiceWithAuthenticationTokenAdded().createPatientForLoggedInUser(patient)

        call.enqueue(object : Callback<PatientWithID> {
            override fun onResponse(
                call: Call<PatientWithID>,
                response: Response<PatientWithID>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    createdPatient = response.body()!!
                    createSensorForPatient()
                }
                else
                {
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

            override fun onFailure(call: Call<PatientWithID>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
            }
        })
    }

    private fun createSensorForPatient() {
        val mainActivityIntent: Intent = Intent(this, MainActivity::class.java)

        // call

        startActivity(mainActivityIntent)
        finish()
    }
}
