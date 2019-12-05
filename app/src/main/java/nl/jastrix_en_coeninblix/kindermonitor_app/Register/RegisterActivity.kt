package nl.jastrix_en_coeninblix.kindermonitor_app.Register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    lateinit var service: APIService
    lateinit var errorfield :TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val buttonregister = findViewById<Button>(R.id.BTNregister)
        val Uname = findViewById<EditText>(R.id.EUname)
        val PW = findViewById<EditText>(R.id.EPW)
        val Fname = findViewById<EditText>(R.id.EVname)
        val Lname = findViewById<EditText>(R.id.ELname)
        val Phone = findViewById<EditText>(R.id.Ephone)
        val Email = findViewById<EditText>(R.id.Eemail)
        errorfield = findViewById<EditText>(R.id.registerError)
        service = apiHelper.buildAndReturnAPIService()
        buttonregister.setOnClickListener() {
            register(
                Uname.text.toString(),
                PW.text.toString(),
                Fname.text.toString(),
                Lname.text.toString(),
                Phone.text.toString(),
                Email.text.toString()
            )
        }
    }


    fun register(
        Uname: String,
        PW: String,
        Fname: String,
        Lname: String,
        Bdate: String,
        Email: String
    ) {
        val userRegister = UserRegister(Uname, PW, Fname, Lname, Bdate, Email)
        val call = apiHelper.buildAndReturnAPIService().userRegister(userRegister)
        call.enqueue(object : Callback<AuthenticationToken> {
            override fun onResponse(call: Call<AuthenticationToken>, response: Response<AuthenticationToken>) {
                val statusCode = response.code()
                if (statusCode == 400){
                    val jObjError = JSONObject(response.errorBody()!!.string())
                    val errorMessage = jObjError.getString("error")

                    errorfield.text = errorMessage
                    errorfield.setBackgroundColor(getColor(R.color.colorBad))
                    errorfield.visibility = View.VISIBLE
                }
                if (response.isSuccessful && response.body() != null){
                    val userData = response.body()!!
                    errorfield.text = "success"
                    errorfield.setBackgroundColor(getColor(R.color.colorGood))
                    errorfield.visibility = View.VISIBLE
                }
                else {
                    if (statusCode == 401) {
                        apiHelper.loginWithCachedUsernameAndPassword()
                    }
                    else{
                        // can only get here if no internet or API is down (get request with only authentication required)
                        // pushnotification is already sent by the sensor function about no connection
                    }
                }
            }

            override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                Log.d("DEBUG", t.message)

                // try again in 5 seconds?
            }
        })

    }

}
