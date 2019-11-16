package nl.jastrix_en_coeninblix.kindermonitor_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        var authToken: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // uncomment next line after testing login and register
//        authToken = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).getString("AuthenticationToken", "")

        if (authToken != "" && authToken != null)
        {
            initHomeActivity();
        }
        else
        {
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initHomeActivity() {

    }
}
