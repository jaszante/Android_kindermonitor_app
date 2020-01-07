package nl.jastrix_en_coeninblix.kindermonitor_app.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authTokenChanged
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterActivity


class LoginActivity : BaseActivityClass(), Callback<AuthenticationToken> {
    private lateinit var loginOrRegisterErrorField: TextView

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private var noCallInProgress: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        this.setTitle(getString(R.string.login))

        loginOrRegisterErrorField = findViewById<TextView>(R.id.loginOrRegisterFailed)
        loginOrRegisterErrorField.visibility = View.INVISIBLE

        val service = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()

        usernameField = findViewById<EditText>(R.id.UserNameField)
        passwordField = findViewById<EditText>(R.id.passwordField)

        val registerButton = findViewById<Button>(nl.jastrix_en_coeninblix.kindermonitor_app.R.id.RegisterButton)
        registerButton.setOnClickListener() {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            if (noCallInProgress) {
                startActivity(registerIntent)
            }
        }

        val loginButton = findViewById<Button>(R.id.LoginButton)
        loginButton.setOnClickListener() {
            loginButton.setBackground(getDrawable(R.drawable.round_shape_dark))
            if (noCallInProgress) {
                noCallInProgress = false
                loginOrRegisterErrorField.visibility = View.INVISIBLE

//            val userLogin = UserLogin(usernameField.text.toString(), passwordField.text.toString())
                val userLogin =
                    UserLogin(usernameField.text.toString(), passwordField.text.toString())
                service.userLogin(userLogin).enqueue(this)
            }
        }
    }

    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
        registerOrLoginFailedShowMessage(getString(R.string.noInternetError))//t.message!!)
    }

    override fun onResponse(call: Call<AuthenticationToken>, response: Response<AuthenticationToken>) {
        noCallInProgress = true
        if (response.isSuccessful && response.body() != null) {
            val monitorApplication = MonitorApplication.getInstance()
            monitorApplication.authToken = response.body()!!.token
            monitorApplication.authTokenChanged = true
            monitorApplication.userName = usernameField.text.toString()
            monitorApplication.password = passwordField.text.toString()

            saveUserCredentials()

            MonitorApplication.getInstance().stopMeasurementService = false

            val patientListIntent = Intent(this, PatientList::class.java)
            startActivity(patientListIntent)
        }
        else {
            val errorbodyLength = response.errorBody()!!.contentLength().toInt()
            if (errorbodyLength != 0) {
                val jObjError = JSONObject(response.errorBody()!!.string())
                val errorMessage = jObjError.getString("error")
                registerOrLoginFailedShowMessage(errorMessage)
            }
            else{
                registerOrLoginFailedShowMessage(response.message())
            }
        }
    }

    private fun saveUserCredentials() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "kinderMonitorApp",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = encryptedSharedPreferences.edit()

        editor.putString("AuthenticationToken", MonitorApplication.getInstance().authToken)
        editor.putString(
            "KinderMonitorAppUserName",
            MonitorApplication.getInstance().userName
        )
        editor.putString(
            "KinderMonitorAppPassword",
            MonitorApplication.getInstance().password
        )
        editor.commit()//apply()
    }

    private fun registerOrLoginFailedShowMessage(message: String) {
        noCallInProgress = true
        loginOrRegisterErrorField.text = message
        loginOrRegisterErrorField.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}