package nl.jastrix_en_coeninblix.kindermonitor_app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.BadResponseFourhundred
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), Callback<AuthenticationToken> {

    private lateinit var loginOrRegisterErrorField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        loginOrRegisterErrorField = findViewById<TextView>(R.id.loginOrRegisterFailed)
        loginOrRegisterErrorField.visibility = View.GONE

        val service = apiHelper.buildAndReturnAPIService()

        val usernameField = findViewById<EditText>(R.id.Username)
        val passwordField = findViewById<EditText>(R.id.password)

        val registerButton = findViewById<Button>(nl.jastrix_en_coeninblix.kindermonitor_app.R.id.RegisterButton)
        registerButton.setOnClickListener() {
            loginOrRegisterErrorField.visibility = View.GONE

//            val gson = Gson()
//            val userRegister = gson.toJson(UserRegister("teff131", "Test-123", "Harrie", "Henry", "404", "Harrie@hotmail.com"))
//            service.userRegister(UserRegister("tef131", "Test-123", "Harrie", "Henry", "404", "Harrie@hotmail.com")).enqueue(this)
            val userRegister = UserRegister("tsff131", "Test-123", "Harrie", "Henry", "404", "Harrie@hotmail.com")
            service.userRegister(userRegister).enqueue(this)
        }

        val loginButton = findViewById<Button>(R.id.LoginButton)
        loginButton.setOnClickListener() {
            loginOrRegisterErrorField.visibility = View.GONE

//            val userLogin = UserLogin(usernameField.text.toString(), passwordField.text.toString())
            val userLogin = UserLogin("tsff131", "Test-123")
            service.userLogin(userLogin).enqueue(this)
        }
    }

    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
        registerOrLoginFailedShowMessage(t.message!!)
    }

    override fun onResponse(call: Call<AuthenticationToken>, response: Response<AuthenticationToken>) {
        if (response.isSuccessful && response.body() != null) {
            authToken = response.body()!!.token

            val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
            editor.putString("AuthenticationToken", authToken)
            editor.apply()

            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else {
//            if (response.code() == 400){
//                val gson = GsonBuilder().create()
//                var fourhundredResponseMessage = gson.fromJson(response.errorBody()!!.string(), BadResponseFourhundred::class.java)
//
//                registerOrLoginFailedShowMessage(fourhundredResponseMessage.text)
//            }
//            else{
                registerOrLoginFailedShowMessage(response.message())
//            }
        }
    }

    private fun registerOrLoginFailedShowMessage(message: String) {
        loginOrRegisterErrorField.text = message
        loginOrRegisterErrorField.visibility = View.VISIBLE
    }
}