package nl.jastrix_en_coeninblix.kindermonitor_app.patientList


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.FirebaseNotifications.MyFirebaseMessagingService
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.currentPatient
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userData
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientListener
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity.Companion.loginWithCachedCredentialsOnResume
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterPatientActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientList : BaseActivityClass() {

    public lateinit var recyclerView: RecyclerView
    public lateinit var viewAdapter: RecyclerView.Adapter<*>
    public lateinit var viewManager: RecyclerView.LayoutManager
    val patientList: ArrayList<PatientWithID> = ArrayList()
    lateinit var text: TextView
    lateinit var buttonPatient: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        this.setTitle(getString(R.string.select_patient))

        val makeSureThereIsAFirebaseToken = MyFirebaseMessagingService()
        makeSureThereIsAFirebaseToken.getFirebaseToken(this)


        val patientListener: PatientListener = object : PatientListener {
            override fun onItemClick(position: Int, patient: PatientWithID) {
                MonitorApplication.getInstance().currentlySelectedPatient = patient
                val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)

                startActivity(mainActivityIntent)
            }
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = PatientAdapter(this, patientList, patientListener)

        recyclerView = findViewById<RecyclerView>(R.id.patientRecyclerview).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //get all patients opnieuw ofzo

                    //viewAdapter.notifyDataSetChanged();
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        MonitorApplication.getInstance().stopMeasurementService = true

        initApp()

        getUserDataThenStartGetPatientsCall()
    }

    private fun initApp() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "kinderMonitorApp",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val authTokenNullable = sharedPreferences.getString("AuthenticationToken", "")
        val userNameNullable = sharedPreferences.getString("KinderMonitorAppUserName", "")
        val passwordNullable = sharedPreferences.getString("KinderMonitorAppPassword", "")

        // if the authtoken, username, password were set earlier the app starts. later if a call fails because of authentication, the app tries to login again with the username password
        // if that succeeds the call is done again this time automatically with the new authentication observableToken, if it fails the user is booted back to login page and has to manually try to log in

        if (authTokenNullable != null && authTokenNullable != ""
            && userNameNullable != null && userNameNullable != ""
            && passwordNullable != null && passwordNullable != ""
        ) {
//            authToken = authTokenNullable
            MonitorApplication.getInstance().userName = userNameNullable
            MonitorApplication.getInstance().password = passwordNullable

//            observableToken.addObserver(this)
//            observableToken.changeToken(authTokenNullable!!) // when observableToken changes userdata call, patients call, and sensors call should be executed in order
            MonitorApplication.getInstance().authTokenChanged = true
        } else {
            removeAllSharedPreferencesAndStartLoginActivity()
        }
    }

    private fun removeAllSharedPreferencesAndStartLoginActivity() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "kinderMonitorApp",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = sharedPreferences.edit()
//        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
        editor.putString("AuthenticationToken", null)
        editor.putString("KinderMonitorAppUserName", null)
        editor.putString("KinderMonitorAppPassword", null)
        editor.apply()

        val loginIntent: Intent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun getUserDataThenStartGetPatientsCall() {
        patientList.clear()

        val loginIntent = Intent(this, LoginActivity::class.java)

        val call =
            MonitorApplication.getInstance().apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    MonitorApplication.getInstance().userData = response.body()!!

                    getAllPatients()
                } else {
                    if (statusCode == 401) {
                        loginWithCachedUsernameAndPassword()
                    }
                    else {
                        removeAllSharedPreferencesAndStartLoginActivity()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                removeAllSharedPreferencesAndStartLoginActivity()
                finish()
            }
        })
    }

    private fun loginWithCachedUsernameAndPassword() {
        val monitorApplication = MonitorApplication.getInstance()
//        val loginIntent = Intent(this, LoginActivity::class.java)
        if (monitorApplication.userName != null && monitorApplication.password != null && monitorApplication.userName != "" && monitorApplication.password != "") {

            val call =
                MonitorApplication.getInstance().apiHelper.buildAndReturnAPIService().userLogin(
                    UserLogin(monitorApplication.userName!!, monitorApplication.password!!)
                )

            call.enqueue(object : Callback<AuthenticationToken> {
                override fun onResponse(
                    call: Call<AuthenticationToken>,
                    response: retrofit2.Response<AuthenticationToken>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val newToken = response.body()!!.token
                        MonitorApplication.getInstance().apiHelper.buildAPIServiceWithNewToken(
                            newToken
                        ) // important that we build the apiservice again with new token before the observabletoken is changed
                        MonitorApplication.getInstance().authToken = newToken
                        getUserDataThenStartGetPatientsCall()
//                        if (MonitorApplication.getInstance().currentlySelectedPatient == null) {
//                            startActivity(patientListIntent)
//                        } else {
//                            getNewUserdataThenInitDrawerWithUserInformation()
//                        }

                    } else {
                        removeAllSharedPreferencesAndStartLoginActivity()
                    }
                }

                override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                    removeAllSharedPreferencesAndStartLoginActivity()
                }
            })
        }
        else {
            removeAllSharedPreferencesAndStartLoginActivity()
        }
    }

    private fun getAllPatients() {
        val loginIntent = Intent(this, LoginActivity::class.java)

        val call = MonitorApplication.getInstance().apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .getAllPatientsForLogginedInUser()
        call.enqueue(object : Callback<Array<PatientWithID>> {
            override fun onResponse(
                call: Call<Array<PatientWithID>>,
                response: Response<Array<PatientWithID>>
            ) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    val allPatients = response.body()!!
                    if (allPatients.count() == 0) {
                        //startActivity(loginIntent)
                        text = findViewById(R.id.TextViewEmpty)
                        buttonPatient = findViewById(R.id.BTNPatient)
                        text.text =
                            "Er zijn op dit moment geen patienten aan uw account gekoppeld. Vraag aan een ouder/verzorger om uw toe te voegen. Of maak een kind aan."
                        buttonPatient.visibility = View.VISIBLE
                        buttonPatient.setOnClickListener{
                            val intent : Intent = Intent(applicationContext, RegisterPatientActivity::class.java)
                            startActivity(intent)
                        }
                        val buttonLoguit = findViewById<Button>(R.id.loguitButton)
                        buttonLoguit.visibility = View.VISIBLE
                        buttonLoguit.setOnClickListener{
                            startActivity(loginIntent)
                        }

                    } else {
                        for (patient in allPatients) {
                            patientList.add(patient)
                        }
                        viewAdapter.notifyDataSetChanged();
                    }

                } else {
                    startActivity(loginIntent)
                }
            }

            override fun onFailure(call: Call<Array<PatientWithID>>, t: Throwable) {
                startActivity(loginIntent)
            }
        })
    }
}
