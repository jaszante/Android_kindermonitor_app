package nl.jastrix_en_coeninblix.kindermonitor_app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import android.util.Log
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*


class LoginActivity : AppCompatActivity(), Callback<AuthenticationToken> {

    private lateinit var loginOrRegisterErrorField: TextView

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private var noCallInProgress: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        loginOrRegisterErrorField = findViewById<TextView>(R.id.loginOrRegisterFailed)
        loginOrRegisterErrorField.visibility = View.GONE

        val service = apiHelper.buildAndReturnAPIService()

        usernameField = findViewById<EditText>(R.id.UserNameField)
        passwordField = findViewById<EditText>(R.id.passwordField)

        val registerButton = findViewById<Button>(nl.jastrix_en_coeninblix.kindermonitor_app.R.id.RegisterButton)
        registerButton.setOnClickListener() {

            // should go to register pagina
        val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)


        }

        val loginButton = findViewById<Button>(R.id.LoginButton)
        loginButton.setOnClickListener() {
            if (noCallInProgress) {
                noCallInProgress = false
                loginOrRegisterErrorField.visibility = View.GONE

//            val userLogin = UserLogin(usernameField.text.toString(), passwordField.text.toString())
                val userLogin =
                    UserLogin(usernameField.text.toString(), passwordField.text.toString())
                service.userLogin(userLogin).enqueue(this)
            }
        }
    }

    private fun getUserDataThenCreatePatientForUserAndGoToMainActivity(){
        val call = apiHelper.buildAPIServiceWithNewToken(observableToken.authToken).getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null){
                    MainActivity.userData = response.body()!!
//                    mainActivity.initDrawerWithUserInformation()
//                    createPatientForThisUser() // moet niet hier, is al gedaan bij registreren of deze gebruiker is
                                                // geen hoofdgebruiker maar kan toegevoegd worden bij iemand met patient
                }
                else {
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    val errorMessage = jObjError.getString("error")
                    registerOrLoginFailedShowMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Log.d("DEBUG", t.message)
                registerOrLoginFailedShowMessage(t.message!!)
            }
        })
    }

    private fun createPatientForThisUser() {
        val intent: Intent = Intent(this, MainActivity::class.java)
        var call = apiHelper.returnAPIServiceWithAuthenticationTokenAdded().createPatientForLoggedInUser(Patient("testus", "testuses", "1982-09-02"))
        call.enqueue(object : Callback<Patient> {
            override fun onResponse(
                call: Call<Patient>,
                response: Response<Patient>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val patient = response.body()!!
                    startActivity(intent)
                }
                else
                {
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    val errorMessage = jObjError.getString("error")
                    registerOrLoginFailedShowMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<Patient>, t: Throwable) {
                registerOrLoginFailedShowMessage(t.message!!)
            }
        })
    }

    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
        registerOrLoginFailedShowMessage(t.message!!)
    }

    override fun onResponse(call: Call<AuthenticationToken>, response: Response<AuthenticationToken>) {
        noCallInProgress = true
        if (response.isSuccessful && response.body() != null) {
            observableToken.changeToken(response.body()!!.token)
            userName = usernameField.text.toString()
            password = passwordField.text.toString()

            val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
            editor.putString("AuthenticationToken", observableToken.authToken)
            editor.putString("KinderMonitorAppUserName", usernameField.text.toString())
            editor.putString("KinderMonitorAppPassword", passwordField.text.toString())
            editor.apply()

            getUserDataThenCreatePatientForUserAndGoToMainActivity()
        }
        else {
//            if (response.code() == 400){
//                val gson = GsonBuilder().create()
//                var fourhundredResponseMessage = gson.fromJson(response.errorBody()!!.string(), BadResponseFourhundred::class.java)
//
//                registerOrLoginFailedShowMessage(fourhundredResponseMessage.text)
//            }
//            else{
            val jObjError = JSONObject(response.errorBody()!!.string())
            val errorMessage = jObjError.getString("error")
                registerOrLoginFailedShowMessage(errorMessage)
//            }
        }
    }

    private fun registerOrLoginFailedShowMessage(message: String) {
        noCallInProgress = true
        loginOrRegisterErrorField.text = message
        loginOrRegisterErrorField.visibility = View.VISIBLE
    }
}