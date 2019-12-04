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
import kotlinx.android.synthetic.main.activity_main.*
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var authToken: String
        lateinit var userName: String
        lateinit var password: String
        val apiHelper = APIHelper()
        lateinit var mainActivity: MainActivity

        //var callsCanBeMade: Boolean = true
    }

    // can be called from APIHelper loginWithCachedUsernameAndPassword function
    fun startLoginAcitivity() {
        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
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

        mainActivity = this

        setupNavigationDrawer();

        val authTokenNullable = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).getString("AuthenticationToken", "")
        val userNameNullable = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).getString("KinderMonitorAppUserName", "")
        val passwordNullable = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).getString("KinderMonitorAppPassword", "")

        // if the authtoken, username, password were set earlier the app starts. later if a call fails because of authentication, the app tries to login again with the username password
        // if that succeeds the call is done again this time automatically with the new authentication token, if it fails the user is booted back to login page and has to manually try to log in
        if (authTokenNullable != null && authTokenNullable != ""
            && userNameNullable != null && userNameNullable != ""
            && passwordNullable != null && passwordNullable != "")
        {
            authToken = authTokenNullable!!
            userName = userNameNullable
            password = passwordNullable

            initDrawerWithUserInformation()
            continuouslyCallForNewMeasurements();
        }
        else
        {
            startLoginAcitivity()
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

    // can be called from APIHelper loginWithCachedUsernameAndPassword function
    fun initDrawerWithUserInformation() {
        val navView = nav_view.getHeaderView(0)
        val navHeaderTitle = navView.findViewById(R.id.navHeaderTitle) as TextView

        val call = apiHelper.returnAPIServiceWithAuthenticationTokenAdded(authToken).getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null){
                    val userData = response.body()!!


                    navHeaderTitle.text = userData.firstName // + " " + userData.LastName
                }
                else {
                    if (statusCode == 401) {
                        apiHelper.loginWithCachedUsernameAndPassword()
                    }
                    else{
                        // can only get here if no internet or API is down (get request with only authentication required)
                        // pushnotification is already sent by the sensor function about no connection
                    }
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Log.d("DEBUG", t.message)

                // try again in 5 seconds?
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

    private fun continuouslyCallForNewMeasurements() {
        // should do new call every second. do not use onresponse to call again, execute new call continuesly while app is in forefront, don't call at all while in background
        // if 5 calls in a row fail (every time a call succeeds it sets the counter to 0, every time it fails it checks the counter for 5 and ++'s that), send alarm that there is no connection with API or internet
    }
}
