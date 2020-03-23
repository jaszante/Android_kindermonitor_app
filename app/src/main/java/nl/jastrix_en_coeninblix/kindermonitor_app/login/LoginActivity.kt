package nl.jastrix_en_coeninblix.kindermonitor_app.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivityClass(), Callback<AuthenticationToken> {
    private lateinit var loginOrRegisterErrorField: TextView

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private var noCallInProgress: Boolean = true
    private lateinit var progressBar: ProgressBar
    var errorlist = ArrayList<String>()
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        this.setTitle(getString(R.string.login))

        loginOrRegisterErrorField = findViewById<TextView>(R.id.loginOrRegisterFailed)
        loginOrRegisterErrorField.visibility = View.INVISIBLE

        val service = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()

        usernameField = findViewById<EditText>(R.id.UserNameField)
        passwordField = findViewById<EditText>(R.id.passwordField)
        usernameField.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        passwordField.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }

        val registerButton =
            findViewById<Button>(nl.jastrix_en_coeninblix.kindermonitor_app.R.id.RegisterButton)
        registerButton.setOnClickListener() {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            if (noCallInProgress) {
                startActivity(registerIntent)
                finish()
            }
        }

        progressBar = findViewById(R.id.progressBar)

        loginButton = findViewById<Button>(R.id.LoginButton)
        loginButton.setOnClickListener() {
            loginButton.setBackground(getDrawable(R.drawable.round_shape_dark))
            if (allFieldsFilledIn()) {
                if (noCallInProgress) {
                    noCallInProgress = false
                    progressBar.visibility = View.VISIBLE
                    loginOrRegisterErrorField.visibility = View.INVISIBLE

//            val userLogin = UserLogin(usernameField.text.toString(), passwordField.text.toString())
                    val userLogin =
                        UserLogin(usernameField.text.toString(), passwordField.text.toString())
                    service.userLogin(userLogin).enqueue(this)
                }
            }
        }
    }

    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
        registerOrLoginFailedShowMessage(getString(R.string.noInternetError))
        progressBar.visibility = View.INVISIBLE
        loginButton.setBackground(getDrawable(R.drawable.rounded_shape))
    }

    override fun onResponse(
        call: Call<AuthenticationToken>,
        response: Response<AuthenticationToken>
    ) {
        noCallInProgress = true
        progressBar.visibility = View.INVISIBLE
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
            finish()
        } else {
            loginButton.setBackground(getDrawable(R.drawable.rounded_shape))
            val errorbodyLength = response.errorBody()!!.contentLength().toInt()
            var errorMessage = "API down"
            if (errorbodyLength != 0) {
                try {

                    val jObjError = JSONObject(response.errorBody()!!.string())
                    errorMessage = jObjError.getString("error")

                } finally {

                }
                registerOrLoginFailedShowMessage(errorMessage)
            } else {
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

    private fun allFieldsFilledIn(): Boolean {
        errorlist.clear()
        if (isNullOrEmpty(usernameField.text.toString())) {
            val string = "gebruikersnaam"
            usernameField.setBackgroundColor(getColor(R.color.colorBad))
            errorlist.add(string)
        }
        if (isNullOrEmpty(passwordField.text.toString())) {
            val string = "wachtwoord"
            passwordField.setBackgroundColor(getColor(R.color.colorBad))
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
            loginOrRegisterErrorField.text = string
            loginOrRegisterErrorField.visibility = View.VISIBLE
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

}