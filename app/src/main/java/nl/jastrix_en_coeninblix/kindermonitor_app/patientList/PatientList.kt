package nl.jastrix_en_coeninblix.kindermonitor_app.patientList


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.FirebaseNotifications.MyFirebaseMessagingService
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientListener
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterPatientActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientList : BaseActivityClass() {

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    val patientList: ArrayList<PatientWithID> = ArrayList()
    lateinit var text: TextView
    lateinit var buttonPatient: Button
    lateinit var progressBar: ProgressBar
    private var noCallInProgress: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        this.setTitle(getString(R.string.select_patient))

        val makeSureThereIsAFirebaseToken = MyFirebaseMessagingService()
        makeSureThereIsAFirebaseToken.getFirebaseToken(this)

        progressBar = findViewById(R.id.progressBar)

        val patientListener: PatientListener = object : PatientListener {
            override fun onItemClick(position: Int, patient: PatientWithID) {
                MonitorApplication.getInstance().currentlySelectedPatient = patient
                val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)

                startActivity(mainActivityIntent)
                finish()
            }
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = PatientAdapter(this, patientList, patientListener)

        recyclerView = findViewById<RecyclerView>(R.id.patientRecyclerview).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
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

        if (authTokenNullable != null && authTokenNullable != ""
            && userNameNullable != null && userNameNullable != ""
            && passwordNullable != null && passwordNullable != ""
        ) {
            MonitorApplication.getInstance().userName = userNameNullable
            MonitorApplication.getInstance().password = passwordNullable

            MonitorApplication.getInstance().authTokenChanged = true
        } else {
            removeAllSharedPreferencesAndStartLoginActivity()
        }
    }

    private fun removeAllSharedPreferencesAndStartLoginActivity() {
        noCallInProgress = true
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "kinderMonitorApp",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = sharedPreferences.edit()
        editor.putString("AuthenticationToken", null)
        editor.putString("KinderMonitorAppUserName", null)
        editor.putString("KinderMonitorAppPassword", null)
        editor.apply()

        val loginIntent: Intent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun getUserDataThenStartGetPatientsCall() {
        if (noCallInProgress) {
            noCallInProgress = false
            patientList.clear()
            progressBar.visibility = View.VISIBLE

            val loginIntent = Intent(this, LoginActivity::class.java)

            val call =
                MonitorApplication.getInstance()
                    .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getCurrentUser()
            call.enqueue(object : Callback<UserData> {
                override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                    val statusCode = response.code()

                    if (response.isSuccessful && response.body() != null) {
                        MonitorApplication.getInstance().userData = response.body()!!

                        getAllPatients()
                    } else {
                        if (statusCode == 401) {
                            loginWithCachedUsernameAndPassword()
                        } else {
                            removeAllSharedPreferencesAndStartLoginActivity()
                        }
                    }
                }

                override fun onFailure(call: Call<UserData>, t: Throwable) {
                    removeAllSharedPreferencesAndStartLoginActivity()
                    noCallInProgress = true
                }
            })
        }
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
                    noCallInProgress = true
                    if (response.isSuccessful && response.body() != null) {
                        val newToken = response.body()!!.token
                        MonitorApplication.getInstance().apiHelper.buildAPIServiceWithNewToken(
                            newToken
                        ) // important that we build the apiservice again with new token before the observabletoken is changed
                        MonitorApplication.getInstance().authToken = newToken
                        getUserDataThenStartGetPatientsCall()

                    } else {
                        removeAllSharedPreferencesAndStartLoginActivity()
                    }
                }

                override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                    removeAllSharedPreferencesAndStartLoginActivity()
                }
            })
        } else {
            removeAllSharedPreferencesAndStartLoginActivity()
        }
    }

    private fun getAllPatients() {
        val loginIntent = Intent(this, LoginActivity::class.java)

        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .getAllPatientsForLogginedInUser()
        call.enqueue(object : Callback<Array<PatientWithID>> {
            override fun onResponse(
                call: Call<Array<PatientWithID>>,
                response: Response<Array<PatientWithID>>
            ) {
                val statusCode = response.code()
                progressBar.visibility = View.INVISIBLE
                noCallInProgress = true
                if (response.isSuccessful && response.body() != null) {
                    val allPatients = response.body()!!
                    if (allPatients.count() == 0) {
                        text = findViewById(R.id.TextViewEmpty)
                        buttonPatient = findViewById(R.id.BTNPatient)
                        text.text =
                            "Er zijn op dit moment geen patienten aan uw account gekoppeld. Vraag aan een ouder/verzorger om uw toe te voegen. Of maak een kind aan."
                        buttonPatient.visibility = View.VISIBLE
                        buttonPatient.setOnClickListener {
                            val intent: Intent =
                                Intent(applicationContext, RegisterPatientActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        val buttonLoguit = findViewById<Button>(R.id.loguitButton)
                        buttonLoguit.visibility = View.VISIBLE
                        buttonLoguit.setOnClickListener {
                            startActivity(loginIntent)
                            finish()
                        }

                    } else {
                        for (patient in allPatients) {
                            patientList.add(patient)
                        }
                        viewAdapter.notifyDataSetChanged();
                    }

                } else {
                    startActivity(loginIntent)
                    finish()
                }
            }

            override fun onFailure(call: Call<Array<PatientWithID>>, t: Throwable) {
                noCallInProgress = true
                startActivity(loginIntent)
                finish()
            }
        })
    }
}
