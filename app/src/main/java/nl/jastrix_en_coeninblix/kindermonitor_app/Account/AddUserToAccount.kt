package nl.jastrix_en_coeninblix.kindermonitor_app.Account

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.RestError
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserObjectWithOnlyUsername
import retrofit2.Retrofit
import java.lang.Exception
import org.json.JSONObject




class AddUserToAccount : BaseActivityClass() {

    lateinit var userToAddEditText: EditText
    lateinit var errorField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user_to_account)
        this.setTitle(R.string.title_add_users)

        // getallusers
        // knop: vergelijk usernames, krijg de userid van een user
        // stuur permission object op naar de function

//        getAllUsers()

        val findAllUserPermissionsButton =  findViewById<Button>(R.id.AddPatientToUserButton)
        findAllUserPermissionsButton.setOnClickListener() {
            errorField.visibility = View.INVISIBLE
            giveUserPermission()
        }
        userToAddEditText = findViewById(R.id.UserToAdd)
        errorField = findViewById(R.id.errorField)
        errorField.visibility = View.INVISIBLE
    }

    private fun giveUserPermission() {
        val loginIntent = Intent(this, LoginActivity::class.java)

        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().giveUserPermission(
            MonitorApplication.getInstance().currentlySelectedPatient!!.patientID, UserObjectWithOnlyUsername(userToAddEditText.text.toString()))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // refresh recyclerview (new call, should get the newly added user as well)

                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        val errorMessage = jObjError.getString("error")
                        errorHandling(errorMessage)

                    }
                    catch (e: Exception) {
                        errorHandling(response.message())
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                errorHandling(t.message!!)
            }

            private fun errorHandling(message: String){
                errorField.text = message
                errorField.visibility = View.VISIBLE
            }
        })
    }
}
