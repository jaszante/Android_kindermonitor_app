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
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.RestError
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserObjectWithOnlyUsername
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import retrofit2.Retrofit
import java.lang.Exception
import org.json.JSONObject




class AddUserToAccount : BaseActivityClass() {

    lateinit var userToAddEditText: EditText
    lateinit var errorField: TextView
    var noCallInProgress = true

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

        getAllUsersWithPermissionForThisPatient()
    }

    private fun giveUserPermission() {
        if (noCallInProgress) {
            noCallInProgress = false
            val loginIntent = Intent(this, LoginActivity::class.java)

            val call = MonitorApplication.getInstance()
                .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().giveUserPermission(
                MonitorApplication.getInstance().currentlySelectedPatient!!.patientID,
                UserObjectWithOnlyUsername(userToAddEditText.text.toString())
            )
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        getAllUsersWithPermissionForThisPatient()
                        noCallInProgress = true
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            errorHandling(errorMessage)

                        } catch (e: Exception) {
                            errorHandling(response.message())
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    errorHandling(t.message!!)
                }

                private fun errorHandling(message: String) {
                    errorField.text = message
                    errorField.visibility = View.VISIBLE
                    noCallInProgress = true
                }
            })
        }
    }

    fun getAllUsersWithPermissionForThisPatient() {
        if (noCallInProgress) {
            noCallInProgress = false
            // haal de oude lijst weg, data.clear()

            val call = MonitorApplication.getInstance()
                .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getAllUsersWithPermission(
                MonitorApplication.getInstance().currentlySelectedPatient!!.patientID
            )
            call.enqueue(object : Callback<ArrayList<UserData>> {
                override fun onResponse(
                    call: Call<ArrayList<UserData>>,
                    response: Response<ArrayList<UserData>>
                ) {
                    if (response.isSuccessful) {
                        noCallInProgress = true
                        // refresh recyclerview, data = responce.body()

                    } else {
                        if (response.code() == 401) {
                            errorHandling(getString(R.string.NoAuthorisationToChangePermissions))
                        } else {
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                val errorMessage = jObjError.getString("error")
                                errorHandling(errorMessage)

                            } catch (e: Exception) {
                                errorHandling(response.message())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ArrayList<UserData>>, t: Throwable) {
                    errorHandling(t.message!!)
                }

                private fun errorHandling(message: String) {
                    noCallInProgress = true
                    errorField.text = message
                    errorField.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun onBackPressed() {
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
        finish()
//        super.onBackPressed()
    }
}
