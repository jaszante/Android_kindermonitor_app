package nl.jastrix_en_coeninblix.kindermonitor_app.register

//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.apiHelper
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authTokenChanged
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
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
    lateinit var uName: EditText
    lateinit var pw: EditText
    lateinit var fName: EditText
    lateinit var lName: EditText
    lateinit var phone: EditText
    lateinit var email: EditText
    lateinit var confirmPW: EditText
    lateinit var progressBar: ProgressBar
    var errorList = ArrayList<String>()

    var noCallInProgress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        this.setTitle(R.string.register_account)
        buttonRegister = findViewById(R.id.BTNregister)
        uName = findViewById(R.id.EUname)
        pw = findViewById(R.id.EPW)
        fName = findViewById(R.id.EVname)
        lName = findViewById(R.id.ELname)
        phone = findViewById(R.id.Ephone)
        email = findViewById(R.id.Eemail)
        checkBoxCaretaker = findViewById(R.id.CBcaretaker)
        errorfield = findViewById<EditText>(R.id.registerError)
        confirmPW = findViewById(R.id.ECPW)
        service = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()
//        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)

//        progressBar = layoutInflater.inflate(R.layout.api_progress_bar, null) as ProgressBar
        progressBar = findViewById(R.id.progressBar)

        /*-------------------------------------------------------------------------------------------*/
        uName.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        pw.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        fName.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        lName.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        phone.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        email.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }
        confirmPW.setOnFocusChangeListener { v, hasFocus ->
            v.background = getDrawable(R.drawable.borderinput)
        }


        buttonRegister.setOnClickListener() {

            if (allFieldsFilledIn()) {
                if (isValidPassword(pw.text.toString())) {
                    if (pw.text.toString() == confirmPW.text.toString()) {
                        buttonRegister.setBackground(getDrawable(R.drawable.round_shape_dark))
                        register(
//                            uName.text.toString(),
//                            "92045FbE",
//                            "92045FbE",
//                            "92045FbE",
//                            "213123565",
//                            "f@e.nl"
                            uName.text.toString(),
                            pw.text.toString(),
                            fName.text.toString(),
                            lName.text.toString(),
                            phone.text.toString(),
                            email.text.toString()
                        )
                    } else {
                        errorfield.text =
                            getString(R.string.wachtwoordenKomenNietOvereen) //"De twee wachtwoorden komen niet overeen"
                        errorfield.visibility = View.VISIBLE
                    }
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
        phoneNumber: String,
        email: String
    ) {
        if (noCallInProgress) {
            noCallInProgress = false
            progressBar.visibility = View.VISIBLE
            val userRegister = UserRegister(uName, pw, fName, lName, phoneNumber, email)
            val call = MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService()
                .userRegister(userRegister)
            call.enqueue(object : Callback<AuthenticationToken> {
                override fun onResponse(
                    call: Call<AuthenticationToken>,
                    response: Response<AuthenticationToken>
                ) {
                    noCallInProgress = true
                    progressBar.visibility = View.INVISIBLE
                    errorfield.visibility = View.INVISIBLE

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
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                val errorMessage = jObjError.getString("error")
                                registerFailedShowMessage(errorMessage)
                            } catch (e: Exception) {
                                registerFailedShowMessage(response.message())
                            } finally {

                            }
                        } else {
                            registerFailedShowMessage(response.message())
                        }
                    }
                    buttonRegister.setBackground(getDrawable(R.drawable.round_shape_dark))
                }

                override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                    Log.d("DEBUG", t.message)
                    noCallInProgress = true
                    progressBar.visibility = View.INVISIBLE

                    registerFailedShowMessage(t.localizedMessage)
                    buttonRegister.setBackground(getDrawable(R.drawable.round_shape_dark))
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

    override fun onBackPressed() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }


    private fun allFieldsFilledIn(): Boolean {
        errorList.clear()
        if (isNullOrEmpty(uName.text.toString())) {
            val string = "gebruikersnaam"
            uName.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(pw.text.toString())) {
            val string = "wachtwoord"
            pw.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(confirmPW.text.toString())) {
            val string = "bevestig wachtwoord"
            confirmPW.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(fName.text.toString())) {
            val string = "voornaam"
            fName.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(lName.text.toString())) {
            val string = "achternaam"
            lName.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(phone.text.toString())) {
            val string = "telefoonnummer"
            phone.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (isNullOrEmpty(email.text.toString())) {
            val string = "email"
            email.setBackgroundColor(getColor(R.color.colorBad))
            errorList.add(string)
        }
        if (errorList.count() > 0) {
            var string = ""
            val max = errorList.count()
            var index = 0
            errorList.forEach {
                if (index < max - 1) {
                    string = string + it + ", "
                } else {
                    string = string + it + " moet(en) worden ingevuld"
                }
                index++
            }
            errorfield.text = string
            errorfield.visibility = View.VISIBLE
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
