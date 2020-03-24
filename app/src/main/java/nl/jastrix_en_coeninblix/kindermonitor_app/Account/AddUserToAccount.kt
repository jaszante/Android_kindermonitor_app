package nl.jastrix_en_coeninblix.kindermonitor_app.Account

import android.content.Intent
import android.media.Image
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_add_user_to_account.*
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.RestError
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserObjectWithOnlyUsername
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientListener
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PermissionAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PermissionListener
import nl.jastrix_en_coeninblix.kindermonitor_app.patientList.PatientList
import nl.jastrix_en_coeninblix.kindermonitor_app.ui.gallery.GalleryFragment
import nl.jastrix_en_coeninblix.kindermonitor_app.ui.share.ShareFragment
import retrofit2.Retrofit
import java.lang.Exception
import org.json.JSONObject


class AddUserToAccount : BaseActivityClass() {

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var userToAddEditText: EditText
    lateinit var errorField: TextView
    var noCallInProgress = true
    var list: ArrayList<UserData> = ArrayList()
    private lateinit var loading: ProgressBar
    private lateinit var findAllUserPermissionsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user_to_account)
        this.setTitle(R.string.title_add_users)
        loading = findViewById(R.id.progressBar)

        // getallusers
        // knop: vergelijk usernames, krijg de userid van een user
        // stuur permission object op naar de function

//        getAllUsers()

        findAllUserPermissionsButton = findViewById<Button>(R.id.AddPatientToUserButton)
        findAllUserPermissionsButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            findAllUserPermissionsButton.setBackground(getDrawable(R.drawable.round_shape_dark))
            giveUserPermission()
        }
        userToAddEditText = findViewById(R.id.UserToAdd)
        errorField = findViewById(R.id.errorField)
        errorField.visibility = View.INVISIBLE


        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onItemClick(position: Int, patient: UserData) {

            }
        }
        viewManager = LinearLayoutManager(this)
        viewAdapter = PermissionAdapter(this, list, permissionListener)

        recyclerView = findViewById<RecyclerView>(R.id.permissionRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

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
                        noCallInProgress = true
                        getAllUsersWithPermissionForThisPatient()

                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            val errorMessage = jObjError.getString("error")
                            errorHandling(errorMessage)

                        } catch (e: Exception) {
                            errorHandling(response.message())
                        }
                    }
                    loading.visibility = View.INVISIBLE
                    findAllUserPermissionsButton.setBackground(getDrawable(R.drawable.rounded_shape))
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    errorHandling(t.message!!)
                    loading.visibility = View.INVISIBLE
                    findAllUserPermissionsButton.setBackground(getDrawable(R.drawable.rounded_shape))
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
            list.clear()

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
                        response.body()!!.forEach {
                            list.add(it)
                        }
                        viewAdapter.notifyDataSetChanged()
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        MonitorApplication.getInstance().usersWithPermissionRecyclerviewShouldBeRefreshed.observe(this, recyclerviewShouldBeRefreshedLiveDataObserver)
    }

    private val recyclerviewShouldBeRefreshedLiveDataObserver = Observer<Boolean> { value ->
        value?.let {
            if (it) {
                getAllUsersWithPermissionForThisPatient()
            }
        }
    }

    override fun onBackPressed() {
        val mainActivity = Intent(this, MainActivity::class.java)
        mainActivity.putExtra("openAccountFragment", true)
        startActivity(mainActivity)
        finish()
    }
}
