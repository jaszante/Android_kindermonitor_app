package com.example.kindermonitor_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kindermonitor_app.login.LoginActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var authToken : String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authToken = getSharedPreferences("AuthenticationToken", Context.MODE_PRIVATE).getString("AuthenticationToken", "")

        if (authToken == "")
        {
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        else
        {
            initHomeActivity();
        }
    }

    private fun initHomeActivity() {

    }
}
