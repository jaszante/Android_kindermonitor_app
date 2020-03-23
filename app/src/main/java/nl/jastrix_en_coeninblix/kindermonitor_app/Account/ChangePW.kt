package nl.jastrix_en_coeninblix.kindermonitor_app.Account

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class ChangePW : BaseActivityClass() {

    private var noCallInProgress: Boolean = true

    private lateinit var passwordEditText: EditText
    private lateinit var passwordCheckEditText: EditText
    private lateinit var errorField: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pw)
        this.setTitle(R.string.title_change_pw)

        errorField = findViewById(R.id.errorField)
        passwordEditText = findViewById(R.id.passwordChangeTextView)
        passwordCheckEditText = findViewById(R.id.checkPassword)
        loading = findViewById(R.id.progressBar)

        changePasswordButton = findViewById(R.id.changeUserDataButton)
        changePasswordButton.setOnClickListener {
            if (isValidPassword(passwordEditText.text.toString())) {
                if (passwordCheckEditText.text.toString() == passwordEditText.text.toString()) {
                    loading.visibility = View.VISIBLE
                    changePasswordButton.setBackground(getDrawable(R.drawable.round_shape_dark))
                    val oldUserData = MonitorApplication.getInstance().userData!!

                    val newUserData = UserRegister(
                        oldUserData.username,
                        passwordEditText.text.toString(),
                        oldUserData.firstName,
                        oldUserData.lastName,
                        oldUserData.phoneNumber,
                        oldUserData.email
                    )
                    updateUserData(newUserData)
                } else {
                    errorField.text = getString(R.string.wachtwoordenKomenNietOvereen)
                    errorField.visibility = View.VISIBLE
                }
            } else {
                errorField.text = getString(R.string.pwError)
                errorField.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUserData(newUserData: UserRegister) {
        errorField.visibility = View.INVISIBLE

        if (noCallInProgress) {
            noCallInProgress = false

            val mainActivityIntent = Intent(this, MainActivity::class.java)

            val call = MonitorApplication.getInstance()
                .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
                .updateCurrentUser(newUserData)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    noCallInProgress = true

                    if (response.isSuccessful) {
                        val oldUserData = MonitorApplication.getInstance().userData!!
                        MonitorApplication.getInstance().userData = UserData(
                            oldUserData.userID,
                            oldUserData.username,
                            passwordEditText.text.toString(),
                            oldUserData.firstName,
                            oldUserData.lastName,
                            oldUserData.phoneNumber,
                            oldUserData.email
                        )
                        updateUserData(newUserData)

                        saveUserCredentials()
                        startActivity(mainActivityIntent)
                        finish()
                    } else {
                        failedMessage(response.message())
                    }
                    loading.visibility = View.INVISIBLE
                    changePasswordButton.setBackground(getDrawable(R.drawable.rounded_shape))
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    loading.visibility = View.INVISIBLE
                    changePasswordButton.setBackground(getDrawable(R.drawable.rounded_shape))
                    failedMessage(t.localizedMessage)
                }

                fun failedMessage(message: String) {
                    noCallInProgress = true
                    errorField.text = message
                    errorField.visibility = View.VISIBLE
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
                        "KinderMonitorAppPassword",
                        passwordEditText.text.toString()
                    )
                    editor.commit()//apply()
                }
            })
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

    override fun onBackPressed() {
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
        finish()
//        super.onBackPressed()
    }
}
