package nl.jastrix_en_coeninblix.kindermonitor_app.register

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
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
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

    lateinit var buttonRegister: Button
    lateinit var checkBoxCaretaker: CheckBox
    lateinit var checkBoxTerms: CheckBox

    lateinit var uName: EditText
    lateinit var pw: EditText
    lateinit var fName: EditText
    lateinit var lName: EditText
    lateinit var phone: EditText
    lateinit var email: EditText

    var noCallInProgress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        buttonRegister = findViewById<Button>(R.id.BTNregister)
        uName = findViewById<EditText>(R.id.EUname)
        pw = findViewById<EditText>(R.id.EPW)
        fName = findViewById<EditText>(R.id.EVname)
        lName = findViewById<EditText>(R.id.ELname)
        phone = findViewById<EditText>(R.id.Ephone)
        email = findViewById<EditText>(R.id.Eemail)
        checkBoxCaretaker = findViewById<CheckBox>(R.id.CBcaretaker)
        checkBoxTerms = findViewById<CheckBox>(R.id.CBterms)
        errorfield = findViewById<EditText>(R.id.registerError)
        service = apiHelper.buildAndReturnAPIService()
        /*-------------------------------------------------------------------------------------------*/
        buttonRegister.setOnClickListener() {
            if (checkBoxTerms.isChecked) {

                register(
                    uName.text.toString(),
                    pw.text.toString(),
                    fName.text.toString(),
                    lName.text.toString(),
                    phone.text.toString(),
                    email.text.toString()
                )
            } else {
                errorfield.text = getString(R.string.registerTerms)
                errorfield.visibility = View.VISIBLE
            }
        }
    }

    private fun register(
        uName: String,
        pw: String,
        fName: String,
        lName: String,
        bDate: String,
        email: String
    ) {
        if (noCallInProgress) {
            noCallInProgress = false
            val userRegister = UserRegister(uName, pw, fName, lName, bDate, email)
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

                        goToRegisterPatientActivityOrMainActivity()

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
                        uName
                    )
                    editor.putString(
                        "KinderMonitorAppPassword",
                        pw
                    )
                    editor.apply()
                }
            })
        }
    }

    fun goToRegisterPatientActivityOrMainActivity(){
        if (checkBoxCaretaker.isChecked) {
            val intent = Intent(this, RegisterPatientActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}
