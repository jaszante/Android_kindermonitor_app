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

class AccountPage : BaseActivityClass() {

    private lateinit var usernameTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var firstnameTextView: EditText
    private lateinit var lastnameTextView: EditText
    private lateinit var phonenumberTextView: EditText
    private lateinit var errorField: TextView
    private lateinit var loading: ProgressBar
    private lateinit var changeUsernameButton: Button

    private var noCallInProgress: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_page)
        this.setTitle(R.string.title_change_cred)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        usernameTextView = findViewById(R.id.PersonGebruikersnaam)
        emailTextView = findViewById(R.id.PersonEmail)
        firstnameTextView = findViewById(R.id.PersonFName)
        lastnameTextView = findViewById(R.id.PersonLName)
        phonenumberTextView = findViewById(R.id.PersonTelefoonNummer)
        errorField = findViewById(R.id.errorField)
        loading = findViewById(R.id.progressBar)

        changeUsernameButton = findViewById(R.id.changeUserDataButton)
        changeUsernameButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            changeUsernameButton.setBackground(getDrawable(R.drawable.round_shape_dark))
            val oldUserData = MonitorApplication.getInstance().userData!!

            val newUserRegister = UserRegister(
                usernameTextView.text.toString(),
                oldUserData.password,
                firstnameTextView.text.toString(),
                lastnameTextView.text.toString(),
                phonenumberTextView.text.toString(),
                emailTextView.text.toString()
            )

            updateUserData(newUserRegister)
        }
    }

    override fun onResume() {
        super.onResume()

        val userdata = MonitorApplication.getInstance().userData!!

        usernameTextView.setText(userdata.username)
        emailTextView.setText(userdata.email)
        firstnameTextView.setText(userdata.firstName)
        lastnameTextView.setText(userdata.lastName)
        phonenumberTextView.setText(userdata.phoneNumber)
    }

    private fun updateUserData(newUserData: UserRegister) {
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
                        val monitorApplication = MonitorApplication.getInstance()

                        val oldUserData = MonitorApplication.getInstance().userData!!
                        monitorApplication.userData = UserData(
                            oldUserData.userID,
                            usernameTextView.text.toString(),
                            oldUserData.password,
                            firstnameTextView.text.toString(),
                            lastnameTextView.text.toString(),
                            phonenumberTextView.text.toString(),
                            emailTextView.text.toString()
                        )
//                        MonitorApplication.getInstance().userRegister.postValue(UserRegister(usernameTextView.text.toString(), oldUserData.password , firstnameTextView.text.toString() ,lastnameTextView.text.toString(), phonenumberTextView.text.toString(), emailTextView.text.toString()))
                        monitorApplication.loggedInUsername.postValue(usernameTextView.text.toString())
                        monitorApplication.loggedInFirstName.postValue(firstnameTextView.text.toString())
                        monitorApplication.loggedInLastName.postValue(lastnameTextView.text.toString())
                        monitorApplication.loggedInEmail.postValue(emailTextView.text.toString())
                        monitorApplication.loggedInPhoneNumber.postValue(phonenumberTextView.text.toString())
                        saveUserCredentials()
                        mainActivityIntent.putExtra("openAccountFragment", true)
                        startActivity(mainActivityIntent)
                        finish()
                    } else {
                        failedMessage(response.message())

                    }
                    errorField.visibility = View.INVISIBLE
                    changeUsernameButton.setBackground(getDrawable(R.drawable.rounded_shape))
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    failedMessage(t.localizedMessage)
                    loading.visibility = View.INVISIBLE
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
                        "KinderMonitorAppUserName",
                        usernameTextView.text.toString()
                    )
                    editor.commit()//apply()
                }
            })
        }
    }

    override fun onBackPressed() {
        val mainActivity = Intent(this, MainActivity::class.java)
        mainActivity.putExtra("openAccountFragment", true)
        startActivity(mainActivity)
        finish()
//        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
