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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authTokenChanged
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterActivity


class LoginActivity : AppCompatActivity(), Callback<AuthenticationToken> {

    companion object {
        var loginWithCachedCredentialsOnResume: Boolean = false
    }
    private lateinit var loginOrRegisterErrorField: TextView

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private var noCallInProgress: Boolean = true

    override fun onResume() {
        super.onResume()
        if (loginWithCachedCredentialsOnResume){
            loginWithCachedUsernameAndPassword()
        }
    }

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
            val registerIntent = Intent(this, RegisterActivity::class.java)
            if (noCallInProgress) {
                startActivity(registerIntent)
            }
//            finish()
        }

        val loginButton = findViewById<Button>(R.id.LoginButton)
        loginButton.setOnClickListener() {
            loginButton.setBackground(getDrawable(R.drawable.round_shape_dark))
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

    fun loginWithCachedUsernameAndPassword() {
        if (noCallInProgress) {
            noCallInProgress = false
            if (userName != null && password != null && userName != "" && password != "") {

                val mainActivityIntent = Intent(this, MainActivity::class.java)

                val call = apiHelper.buildAndReturnAPIService().userLogin(UserLogin(userName, password))

                call.enqueue(object : Callback<AuthenticationToken> {
                    override fun onResponse(call: Call<AuthenticationToken>, response: retrofit2.Response<AuthenticationToken>) {
                        if (response.isSuccessful && response.body() != null){

                            val newToken = response.body()!!.token
                            apiHelper.buildAPIServiceWithNewToken(newToken) // important that we build the apiservice again with new token before the observabletoken is changed
//                            observableToken.changeToken(newToken)
                            startActivity(mainActivityIntent)
                            loginWithCachedCredentialsOnResume = false
                            noCallInProgress = true

                        }
                        else {
                            logOutUser()
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                        logOutUser()
                    }

                    private fun logOutUser() {
                        noCallInProgress = true

                        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                        val sharedPreferences = EncryptedSharedPreferences.create(
                            "kinderMonitorApp",
                            masterKeyAlias,
                            applicationContext,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )

                        val editor = sharedPreferences.edit()
//        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
                        editor.putString("AuthenticationToken", null)
                        editor.putString("KinderMonitorAppUserName", null)
                        editor.putString("KinderMonitorAppPassword", null)
                        editor.apply()

//                        val intent: Intent = Intent(this, LoginActivity::class.java)
//                        startActivity(intent)
                    }
                })
            }
        }
    }

    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
        registerOrLoginFailedShowMessage(t.message!!)
    }

    override fun onResponse(call: Call<AuthenticationToken>, response: Response<AuthenticationToken>) {
        noCallInProgress = true
        if (response.isSuccessful && response.body() != null) {
            authToken = response.body()!!.token
            authTokenChanged = true
            userName = usernameField.text.toString()
            password = passwordField.text.toString()

            saveUserCredentials()
//            val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
//            editor.putString("AuthenticationToken", authToken)
//            editor.putString("KinderMonitorAppUserName", usernameField.text.toString())
//            editor.putString("KinderMonitorAppPassword", passwordField.text.toString())
//            editor.apply()

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)

//            getUserDataThenCreatePatientForUserAndGoToMainActivity()
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

        val sharedPreferences = EncryptedSharedPreferences.create(
            "kinderMonitorApp",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = sharedPreferences.edit()

        editor.putString("AuthenticationToken", authToken)
        editor.putString(
            "KinderMonitorAppUserName",
            userName
        )
        editor.putString(
            "KinderMonitorAppPassword",
            password
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