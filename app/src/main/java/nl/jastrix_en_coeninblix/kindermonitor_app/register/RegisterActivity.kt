package nl.jastrix_en_coeninblix.kindermonitor_app.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authTokenChanged
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


class RegisterActivity : BaseActivityClass() {
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
        service = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()

        /*-------------------------------------------------------------------------------------------*/
        buttonRegister.setOnClickListener() {
            if (checkBoxTerms.isChecked) {

                if (isValidPassword(pw.text.toString())) {

                    register(
                        uName.text.toString(),
                        pw.text.toString(),
                        fName.text.toString(),
                        lName.text.toString(),
                        phone.text.toString(),
                        email.text.toString()
                    )
                } else {
                    errorfield.text = getString(R.string.pwError)
                    errorfield.visibility = View.VISIBLE
                }
//                register(
//                    uName.text.toString(),
//                    "kees123213214455",
//                    "a",
//                   "B",
//                   "123",
//                   "kees@kees.kees"
//                )
            } else {
                errorfield.text = getString(R.string.registerTerms)
                errorfield.visibility = View.VISIBLE
            }
        }
    }

    private fun isValidPassword(pw: String, updateUI: Boolean = true): Boolean {
        val str: CharSequence = pw
        var valid = true

        if (str.length < 8) {
            return false
        }

        var numberTotal: Int = 0
        for (char: Char in str) {
            try {
                char.toInt()
                numberTotal++
            } finally {
                // nothing
            }
        }

        if (numberTotal <= 2) {
            return false
        }

        // Password should contain at least one capital letter
        val exp = ".*[A-Z].*"
        val pattern = Pattern.compile(exp)
        val matcher = pattern.matcher(str)
        if (!matcher.matches()) {
            return false
        }

        return true
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
            val call = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()
                .userRegister(userRegister)
            call.enqueue(object : Callback<AuthenticationToken> {
                override fun onResponse(
                    call: Call<AuthenticationToken>,
                    response: Response<AuthenticationToken>
                ) {
                    noCallInProgress = true

                    if (response.isSuccessful && response.body() != null) {
                        MonitorApplication.getInstance().authToken = response.body()!!.token
                        MonitorApplication.getInstance().apiHelper.buildAPIServiceWithNewToken(
                            response.body()!!.token
                        )
                        saveUserCredentials()

                        goToRegisterPatientActivityOrPatientListActivity()

                    } else {
                        val errorbodyLength = response.errorBody()!!.contentLength().toInt()
                        if (errorbodyLength != 0) {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            registerFailedShowMessage(errorMessage)
                        } else {
                            registerFailedShowMessage(response.message())
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                    Log.d("DEBUG", t.message)
                    noCallInProgress = true

                    registerFailedShowMessage(t.localizedMessage)
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
                    editor.putString(
                        "AuthenticationToken",
                        MonitorApplication.getInstance().authToken
                    )
                    editor.putString(
                        "KinderMonitorAppUserName",
                        uName
                    )
                    editor.putString(
                        "KinderMonitorAppPassword",
                        pw
                    )
                    editor.commit()//apply()
                }
            })
        }
    }

    private fun registerFailedShowMessage(errorMessage: String) {
//        noCallInProgress = true
        errorfield.text = errorMessage
        errorfield.visibility = View.VISIBLE
    }

    fun goToRegisterPatientActivityOrPatientListActivity() {
        MonitorApplication.getInstance().authTokenChanged = true
        if (checkBoxCaretaker.isChecked) {
            val registerPatientIntent = Intent(this, RegisterPatientActivity::class.java)
            startActivity(registerPatientIntent)
            finish()
        } else {
            val patientListIntent = Intent(this, PatientList::class.java)
            startActivity(patientListIntent)
            finish()
        }
    }
}
