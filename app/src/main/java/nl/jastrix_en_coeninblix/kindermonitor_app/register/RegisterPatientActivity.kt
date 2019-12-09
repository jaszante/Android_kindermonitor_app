package nl.jastrix_en_coeninblix.kindermonitor_app.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Patient
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterPatientActivity : AppCompatActivity() {

    var noCallInProgress: Boolean = true
    lateinit var registerPatientButton: Button
    lateinit var patientFirstNameEditText: EditText
    lateinit var patientLastNameEditText: EditText
    lateinit var patientBirthDateEditText: EditText
    lateinit var patientRegisterErrorField: TextView

    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_patient)

        // after startactivity do finish()
        patientFirstNameEditText = findViewById(R.id.patientFirstName)
        patientLastNameEditText = findViewById(R.id.patientLastName)
        patientBirthDateEditText = findViewById(R.id.patientBirthDate)
        patientRegisterErrorField = findViewById((R.id.patientRegisterError))

        registerPatientButton = findViewById(R.id.patientRegisterButton)
        registerPatientButton.setOnClickListener() {

        }
    }

    private fun getUserDataThenCreatePatientForUserAndGoToMainActivity(){
        val call = MainActivity.apiHelper.buildAPIServiceWithNewToken(authToken).getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                if (response.isSuccessful && response.body() != null){
                    userData = response.body()!!
//                    createPatientForThisUser() // moet niet hier, is al gedaan bij registreren of deze gebruiker is
                    // geen hoofdgebruiker maar kan toegevoegd worden bij iemand met patient
                }
                else {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        registerPatientShowErrorMessage(errorMessage)
                    }
                    else{
                        registerPatientShowErrorMessage(response.message())
                    }
//                    registerOrLoginFailedShowMessage(errorMessage)
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

    private fun createPatientForThisUser() {
        val mainActivityIntent: Intent = Intent(this, MainActivity::class.java)
        var call = MainActivity.apiHelper.returnAPIServiceWithAuthenticationTokenAdded().createPatientForLoggedInUser(
            Patient("testus", "testuses", "1982-09-02")
        )
        call.enqueue(object : Callback<Patient> {
            override fun onResponse(
                call: Call<Patient>,
                response: Response<Patient>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val patient = response.body()!!
                    startActivity(mainActivityIntent)
//                    finish()
                }
                else
                {
                    val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                    if (errorbodyLength != 0) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        registerPatientShowErrorMessage(errorMessage)
                    }
                    else{
                        registerPatientShowErrorMessage(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Patient>, t: Throwable) {
                registerPatientShowErrorMessage(t.message!!)
            }
        })
    }
}
