package nl.jastrix_en_coeninblix.kindermonitor_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*
import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity.Companion.loginWithCachedCredentialsOnResume
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
//import nl.jastrix_en_coeninblix.kindermonitor_app.ui.home.HomeFragment.Companion.patientSensors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : BaseActivityClass(), Observer {
    override fun update(o: Observable?, arg: Any?) {
//        getNewUserdataThenInitDrawerWithUserInformation()
    }

//    companion object {
////        lateinit var userName: String
////        lateinit var password: String
////        val apiHelper = APIHelper()
////        lateinit var userData: UserData
//
////        var authToken: String = ""
////        var authTokenChanged: Boolean = false
//
////        var active: Boolean = false
////        var currentPatient : PatientWithID? = null
//    }

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

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigationDrawer();

        MonitorApplication.getInstance().fragmentManager = supportFragmentManager
    }

    private fun setupNavigationDrawer() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_share //, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
//        active = true

        if (MonitorApplication.getInstance().authTokenChanged) {
            // THE USERDATA AND PATIENT CALLS SHOULD BE DONE IN NEW PATIENT OVERVIEW ACTIVITY.
            // IN THAT ACTIVITY THE CURRENTPATIENT IS CHOSEN AND SET TO THE COMPANION HERE, THEN THE MESUREMENT CALLS CAN START
            getNewUserdataThenInitDrawerWithUserInformation()
        } else {
            initDrawerWithUserInformationThenGetPatientSensors()
        }
    }

    private fun getNewUserdataThenInitDrawerWithUserInformation() {
        val loginIntent = Intent(this, LoginActivity::class.java)

        val call = MonitorApplication.getInstance()
            .apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    MonitorApplication.getInstance().userData = response.body()!!

                    initDrawerWithUserInformationThenGetPatientSensors()
                } else {
//                    if (statusCode == 401) {
//                        loginWithCachedUsernameAndPassword()
//                    } else {
                    removeAllSharedPreferencesAndStartLoginActivity()
//                    }

                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                startActivity(loginIntent)
            }
        })
    }

    private fun initDrawerWithUserInformationThenGetPatientSensors() {
        val navView = nav_view.getHeaderView(0)
        val navHeaderTitle = navView.findViewById(R.id.navHeaderTitle) as TextView
        val patientTitle = navView.findViewById(R.id.navHeaderBottomText) as TextView
        patientTitle.text =
            getString(R.string.nav_header_patient) + " " + MonitorApplication.getInstance().currentlySelectedPatient!!.firstname + " " + MonitorApplication.getInstance().currentlySelectedPatient!!.lastname
        navHeaderTitle.text =
            getString(R.string.nav_header_name) +" "+ MonitorApplication.getInstance().userData!!.username

        // + " " + userData.LastName
        MonitorApplication.getInstance().authTokenChanged = false
        getPatientSensors()

    }

//    override fun onStop() {
//        active = false
//        super.onStop()
//    }


    // called after patient has been chosen
    private fun getPatientSensors() {
        val patientListIntent = Intent(this, PatientList::class.java)
        val loginIntent = Intent(this, LoginActivity::class.java)

        if (MonitorApplication.getInstance().currentlySelectedPatient != null) {
            val call = MonitorApplication.getInstance()
                .apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
                .getPatientsSensors(MonitorApplication.getInstance().currentlySelectedPatient!!.patientID.toString())
            call.enqueue(object : Callback<Array<SensorFromCallback>> {
                override fun onResponse(
                    call: Call<Array<SensorFromCallback>>,
                    response: Response<Array<SensorFromCallback>>
                ) {
                    val statusCode = response.code()

                    if (response.isSuccessful && response.body() != null) {
                        val monitorApplication = MonitorApplication.getInstance()
                        for (sensorFromCallback in response.body()!!) {
//                            var patientSensor: PatientSensor? = null
                            when (sensorFromCallback.type) {
                                SensorType.Hartslag.toString() -> {
                                    monitorApplication.hartslagSensor = PatientSensor(
                                        sensorFromCallback.sensorID, SensorType.Hartslag,
                                        sensorFromCallback.thresholdMin,
                                        sensorFromCallback.thresholdMax
                                    )

                                }
                                SensorType.Temperature.toString() -> {
                                    monitorApplication.temperatuurSensor =
                                        PatientSensor(
                                            sensorFromCallback.sensorID,
                                            SensorType.Temperature,
                                            sensorFromCallback.thresholdMin,
                                            sensorFromCallback.thresholdMax
                                        )
//                                    monitorApplication.thresholdTemperatuurValues = ThresholdValues(sensorFromCallback.thresholdMin, sensorFromCallback.thresholdMax)
                                }
                                SensorType.Adem.toString() -> {
                                    monitorApplication.ademFrequentieSensor =
                                        PatientSensor(
                                            sensorFromCallback.sensorID, SensorType.Adem,
                                            sensorFromCallback.thresholdMin,
                                            sensorFromCallback.thresholdMax
                                        )
//                                    monitorApplication.thresholdAdemFrequentieValues = ThresholdValues(sensorFromCallback.thresholdMin, sensorFromCallback.thresholdMax)

                                }
                                SensorType.Saturatie.toString() -> {
                                    monitorApplication.saturatieSensor = PatientSensor(
                                        sensorFromCallback.sensorID, SensorType.Saturatie,
                                        sensorFromCallback.thresholdMin,
                                        sensorFromCallback.thresholdMax
                                    )
                                }
                            }
                        }
                        if (monitorApplication.hartslagSensor != null &&
                            monitorApplication.temperatuurSensor != null &&
                            monitorApplication.ademFrequentieSensor != null &&
                            monitorApplication.saturatieSensor != null
                        ) {
                            monitorApplication.startForegroundMeasurmentService()
                        }
                    } else {
//                        if (statusCode == 401) {
////                            loginWithCachedCredentialsOnResume = true
////                            startActivity(loginIntent)
//                        } else if (statusCode == 404) {
//                            // notification that there is no connection to API
//                        } else {
//                            // internet down notification? // or maybe there are no patients linked?
//                        }

                        removeAllSharedPreferencesAndStartLoginActivity()
                    }
                }

                override fun onFailure(call: Call<Array<SensorFromCallback>>, t: Throwable) {
                    Log.d("DEBUG", t.message)
                    removeAllSharedPreferencesAndStartLoginActivity()
                }
            })
        } else {
            startActivity(patientListIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
