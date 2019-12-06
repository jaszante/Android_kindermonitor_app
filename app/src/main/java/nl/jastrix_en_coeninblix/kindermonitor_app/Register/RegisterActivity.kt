package nl.jastrix_en_coeninblix.kindermonitor_app.Register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.android.synthetic.main.login_activity.*
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    lateinit var service: APIService
    lateinit var errorfield: TextView
    var noCallInProgress = true
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
        val checkboxcaretaker = findViewById<CheckBox>(R.id.CBcaretaker)
        val checkBoxterms = findViewById<CheckBox>(R.id.CBterms)
        errorfield = findViewById<EditText>(R.id.registerError)
        service = apiHelper.buildAndReturnAPIService()
        /*-------------------------------------------------------------------------------------------*/
        buttonregister.setOnClickListener() {
            if (checkBoxterms.isChecked) {

                register(
                    Uname.text.toString(),
                    PW.text.toString(),
                    Fname.text.toString(),
                    Lname.text.toString(),
                    Phone.text.toString(),
                    Email.text.toString()
                )
            } else {
                errorfield.text = getString(R.string.registerTerms)
                errorfield.visibility = View.VISIBLE

            }

            if (checkboxcaretaker.isChecked) {
                /* ga naar kind scherm */

            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
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
        if (noCallInProgress) {
            noCallInProgress = false
            val userRegister = UserRegister(Uname, PW, Fname, Lname, Bdate, Email)
            val call = apiHelper.buildAndReturnAPIService().userRegister(userRegister)
            call.enqueue(object : Callback<AuthenticationToken> {
                override fun onResponse(
                    call: Call<AuthenticationToken>,
                    response: Response<AuthenticationToken>
                ) {
                    noCallInProgress = true
                    val statusCode = response.code()

                    if (response.isSuccessful && response.body() != null) {
                        saveUserCredentials()
                    } else {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")

                        errorfield.text = errorMessage
                        errorfield.visibility = View.VISIBLE

                    }
                }

                override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                    Log.d("DEBUG", t.message)
                    noCallInProgress = false

                    // try again in 5 seconds?
                }

                fun saveUserCredentials() {
                    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                    val sharedPreferences = EncryptedSharedPreferences.create(
                        "kinderMonitorApp",
                        masterKeyAlias,
                        applicationContext,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )

                    val editor = sharedPreferences.edit()

//                        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
                    editor.putString("AuthenticationToken", observableToken.authToken)
                    editor.putString(
                        "KinderMonitorAppUserName",
                        Uname
                    )
                    editor.putString(
                        "KinderMonitorAppPassword",
                        PW
                    )
                    editor.apply()
                }


            })
        }

    }

}
