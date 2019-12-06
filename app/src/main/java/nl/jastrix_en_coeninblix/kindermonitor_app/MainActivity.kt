package nl.jastrix_en_coeninblix.kindermonitor_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.android.synthetic.main.activity_main.*
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.Sensor
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.observableToken.ObservableToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), Observer {
    override fun update(o: Observable?, arg: Any?) {
//        initDrawerWithUserInformation()
    }

    companion object {
        //        lateinit var authToken: String
//        var authToken : String by Delegates.observable("observableToken observable") {
//                _, oldValue, newValue ->
//            onTokenChange?.invoke(oldValue, newValue)
//
//        }
//        var onTokenChange: ((String, String) -> Unit)? = null
        val observableToken: ObservableToken = ObservableToken()// tokenTest: ObservableToken()

        lateinit var userName: String
        lateinit var password: String
        val apiHelper = APIHelper()
        lateinit var userData: UserData
        lateinit var mainAcitivityContext: Context

        var authTokenChanged: Boolean = false
    }

    // can be called from APIHelper loginWithCachedUsernameAndPassword function
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

        val intent: Intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainAcitivityContext = this

        setupNavigationDrawer();

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

//        val authTokenNullable = getSharedPreferences(
//            "kinderMonitorApp",
//            Context.MODE_PRIVATE
//        ).getString("AuthenticationToken", "")
////        val userNameNullable = getSharedPreferences(
////            "kinderMonitorApp",
////            Context.MODE_PRIVATE
////        ).getString("KinderMonitorAppUserName", "")
//        val passwordNullable = getSharedPreferences(
//            "kinderMonitorApp",
//            Context.MODE_PRIVATE
//        ).getString("KinderMonitorAppPassword", "")

        // if the authtoken, username, password were set earlier the app starts. later if a call fails because of authentication, the app tries to login again with the username password
        // if that succeeds the call is done again this time automatically with the new authentication observableToken, if it fails the user is booted back to login page and has to manually try to log in
        if (authTokenNullable != null && authTokenNullable != ""
            && userNameNullable != null && userNameNullable != ""
            && passwordNullable != null && passwordNullable != ""
        ) {
//            authToken = authTokenNullable
            userName = userNameNullable
            password = passwordNullable

//            observableToken.addObserver(this)
            observableToken.changeToken(authTokenNullable!!) // when observableToken changes userdata call, patients call, and sensors call should be executed in order
            authTokenChanged = true
        } else {
            removeAllSharedPreferencesAndStartLoginActivity()
        }
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
                R.id.nav_tools, R.id.nav_share //, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()

        if (authTokenChanged){
            initDrawerWithUserInformation()
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//        val authTokenNullable = getSharedPreferences(
//            "kinderMonitorApp",
//            Context.MODE_PRIVATE
//        ).getString("AuthenticationToken", "")
//        val userNameNullable = getSharedPreferences(
//            "kinderMonitorApp",
//            Context.MODE_PRIVATE
//        ).getString("KinderMonitorAppUserName", "")
//        val passwordNullable = getSharedPreferences(
//            "kinderMonitorApp",
//            Context.MODE_PRIVATE
//        ).getString("KinderMonitorAppPassword", "")
//
//
//        if (authTokenNullable == null || authTokenNullable == ""
//            && userNameNullable == null || userNameNullable == ""
//            && passwordNullable == null || passwordNullable == ""
//        ) {
//
//        }
//    }


    // comment after this no longer relevant (for now) // can be called from APIHelper loginWithCachedUsernameAndPassword function and from loginacitivity after succesful login / register (to update the logged in user's data in the drawer)
    private fun initDrawerWithUserInformation() {
        val navView = nav_view.getHeaderView(0)
        val navHeaderTitle = navView.findViewById(R.id.navHeaderTitle) as TextView

        val call = apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    userData = response.body()!!
//                    getPatientSensors()

                    navHeaderTitle.text = userData.username // + " " + userData.LastName
                    authTokenChanged = false
                } else {
                    if (statusCode == 401) {
                        apiHelper.loginWithCachedUsernameAndPassword() // add authTokenChanged at the end of this function if that works
                        // after observableToken is changed, this function needs to be called again
                    }
                    // else is not needed because you can only get a different status code if there is no internet connection or API is down
                    // push notification should be sent by the continues measurement calls about no connection
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Log.d("DEBUG", t.message)
                // getting here means no internet / API down. Should already be handled by the continues measurement calls
            }
        })

    }

    // called after userdata has been recieved
    private fun getPatientSensors() {
        val call = MainActivity.apiHelper.returnAPIServiceWithAuthenticationTokenAdded()
            .getPatientsSensors(userData.userID.toString())
        call.enqueue(object : Callback<Array<Sensor>> {
            override fun onResponse(
                call: Call<Array<Sensor>>,
                response: Response<Array<Sensor>>
            ) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    val sensors: Array<Sensor> = response.body()!!
                } else {
                    if (statusCode == 401) {
                        apiHelper.loginWithCachedUsernameAndPassword()
                    } else if (statusCode == 404) {
                        // notification that there is no connection to API
                    } else {
                        // internet down notification
                    }
                    // try again
                    Timer("scheduleAfterOneSecond", false).schedule(1000) {
                        getPatientSensors()
                    }

                }
            }

            override fun onFailure(call: Call<Array<Sensor>>, t: Throwable) {
                Log.d("DEBUG", t.message)

                // internet down notification
            }
        })
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
