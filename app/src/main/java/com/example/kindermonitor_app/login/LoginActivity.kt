package com.example.kindermonitor_app.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kindermonitor_app.R
//import com.example.kindermonitor_app.Compantion.authToken

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
    }

    // authToken = body.response()!!.authToken // set authtoken on mainActivity
    //        val editor = getSharedPreferences("assignmentAppAuthToken", Context.MODE_PRIVATE).edit()
//        editor.putString("AuthToken", authToken)
//        editor.apply()
    // go to main activity
}