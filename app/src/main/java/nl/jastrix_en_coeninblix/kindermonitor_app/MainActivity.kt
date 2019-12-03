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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIHelper
import nl.jastrix_en_coeninblix.kindermonitor_app.api.APIService
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Debug
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var authToken: String
        val apiHelper = APIHelper()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigationDrawer();

         // uncomment next line after testing login and register
        val authTokenNullable = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).getString("AuthenticationToken", "")

        if (authTokenNullable != "" && authTokenNullable != null)
        {
            authToken = authTokenNullable!!
            initDrawerWithUserInformation()
            initHomeActivity();
        }
        else
        {
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun initDrawerWithUserInformation() {
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
                    // try again in 5 seconds?
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

    private fun initHomeActivity() {

    }
}
