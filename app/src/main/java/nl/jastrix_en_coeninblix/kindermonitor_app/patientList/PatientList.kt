package nl.jastrix_en_coeninblix.kindermonitor_app.patientList


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.currentPatient
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientAdapter
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters.PatientListener

class PatientList : AppCompatActivity() {

    public lateinit var recyclerView: RecyclerView
    public lateinit var viewAdapter: RecyclerView.Adapter<*>
    public lateinit var viewManager: RecyclerView.LayoutManager
    val patientList: ArrayList<PatientWithID> = ArrayList()

    val patient1 = PatientWithID(1,"karel","kees", "10-10-10")
    val patient2 = PatientWithID(2,"henk","de snoepert", "10-10-10")
    val patient3 = PatientWithID(3,"harry","de banaan", "10-10-10")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
//----------------------------------------------------------------------------------------------------------------------
        // vul de lijst voor lokaal
        patientList.add(patient1)
        patientList.add(patient2)
        patientList.add(patient3)
//----------------------------------------------------------------------------------------------------------------------
        
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
                }
            }

        })
    }
}
