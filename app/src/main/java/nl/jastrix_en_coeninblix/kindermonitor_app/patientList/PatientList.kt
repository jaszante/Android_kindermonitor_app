package nl.jastrix_en_coeninblix.kindermonitor_app.patientList


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.currentPatient
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userData
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientListener
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
//import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity.Companion.loginWithCachedCredentialsOnResume
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterPatientActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientList : AppCompatActivity() {

    public lateinit var recyclerView: RecyclerView
    public lateinit var viewAdapter: RecyclerView.Adapter<*>
    public lateinit var viewManager: RecyclerView.LayoutManager
    val patientList: ArrayList<PatientWithID> = ArrayList()
    lateinit var text: TextView
    lateinit var buttonPatient: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        val patientListener: PatientListener = object : PatientListener {
            override fun onItemClick(position: Int, patient: PatientWithID) {
                //Log.d("DEBUG", articlesFromResponse[position].toString())

                currentPatient = patient
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

        getUserDataThenStartGetPatientsCall()
    }

    private fun getUserDataThenStartGetPatientsCall() {
        val loginIntent = Intent(this, LoginActivity::class.java)

        val call =
            MonitorApplication.getInstance().apiHelper.returnAPIServiceWithAuthenticationTokenAdded().getCurrentUser()
        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                val statusCode = response.code()

                if (response.isSuccessful && response.body() != null) {
                    userData = response.body()!!

                    getAllPatients()
                } else {

                    if (statusCode == 401) {
                        MonitorApplication.getInstance().loginWithCachedCredentialsOnResume = true
                    }

                    startActivity(loginIntent)
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                startActivity(loginIntent)
            }
        })
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
