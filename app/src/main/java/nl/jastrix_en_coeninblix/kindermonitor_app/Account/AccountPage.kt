package nl.jastrix_en_coeninblix.kindermonitor_app.Account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountPage : BaseActivityClass() {

//    val


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_page)
        this.setTitle(R.string.title_change_cred)


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
                        saveUserCredentials()
                        startActivity(mainActivityIntent)
                    }
                    else{
                        failedMessage(response.message())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    failedMessage(t.localizedMessage)
                }

                fun failedMessage(message: String){
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
                        uName
                    )
                    editor.commit()//apply()
                }
            })
        }
    }
}
