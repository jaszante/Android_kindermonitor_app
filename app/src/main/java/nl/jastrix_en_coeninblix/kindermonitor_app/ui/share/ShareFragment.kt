package nl.jastrix_en_coeninblix.kindermonitor_app.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.Account.AccountPage
import nl.jastrix_en_coeninblix.kindermonitor_app.Account.AddUserToAccount
import nl.jastrix_en_coeninblix.kindermonitor_app.Account.ChangePW
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.register.RegisterPatientActivity

class ShareFragment : Fragment() {

    private lateinit var shareViewModel: ShareViewModel

    private lateinit var usernameTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneNumberTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shareViewModel =
            ViewModelProviders.of(this).get(ShareViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_share, container, false)
//        val textView: TextView = root.findViewById(R.id.text_share)
//        shareViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentView = getView()!!

        usernameTextView = currentView.findViewById(R.id.PersonGebruikersnaam)
        firstNameTextView = currentView.findViewById(R.id.PersonFName)
        lastNameTextView = currentView.findViewById(R.id.PersonLName)
        emailTextView = currentView.findViewById(R.id.PersonEmail)
        phoneNumberTextView = currentView.findViewById(R.id.PersonTelefoonNummer)

        val buttonLogout = currentView.findViewById<Button>(R.id.BTNlogout)
        buttonLogout.setOnClickListener {
            buttonLogout.setBackground(
                getDrawable(
                    currentView.context,
                    R.drawable.round_shape_dark
                )
            )
            MonitorApplication.getInstance().stopMeasurementService = true
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            val sharedPreferences = EncryptedSharedPreferences.create(
                "kinderMonitorApp",
                masterKeyAlias,
                activity!!.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val editor = sharedPreferences.edit()

//                        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
            editor.putString("AuthenticationToken", null)
            editor.putString("KinderMonitorAppUserName", null)
            editor.putString("KinderMonitorAppPassword", null)
            editor.apply()

            val loginIntent: Intent = Intent(activity, LoginActivity::class.java)
            startActivity(loginIntent)
            activity!!.finish()
        }

        val addPatientLayout = currentView.findViewById<LinearLayout>(R.id.Add_patient)
        addPatientLayout.setOnClickListener {
            val registerPatientIntent = Intent(activity, RegisterPatientActivity::class.java)
            registerPatientIntent.putExtra("cameFromAccountFragment", true)
            startActivity(registerPatientIntent)
            activity!!.finish()
        }
        val changePwLayout = currentView.findViewById<LinearLayout>(R.id.ChangePWLayout)
        changePwLayout.setOnClickListener {
            val changePWIntent = Intent(activity, ChangePW::class.java)
            startActivity(changePWIntent)
            activity!!.finish()
        }

        val changeCredLayout = currentView.findViewById<LinearLayout>(R.id.ChangeCredentialsLayout)
        changeCredLayout.setOnClickListener {
            val changeCredIntent = Intent(activity, AccountPage::class.java)
            startActivity(changeCredIntent)
            activity!!.finish()
        }
        val manageGebruikersLayout =
            currentView.findViewById<LinearLayout>(R.id.ManageGebruikersLayout)
        manageGebruikersLayout.setOnClickListener {
            val intent = Intent(activity, AddUserToAccount::class.java)
            startActivity(intent)
            activity!!.finish()
        }
    }

    private val changeUsernameLiveDataObserver = Observer<String> { value ->
        value?.let { usernameTextView.text = it }
    }

    private val changeFirstNameLiveDataObserver = Observer<String> { value ->
        value?.let { firstNameTextView.text = it }
    }

    private val changeLastNameLiveDataObserver = Observer<String> { value ->
        value?.let { lastNameTextView.text = it }
    }

    private val changeEmailLiveDataObserver = Observer<String> { value ->
        value?.let { emailTextView.text = it }
    }

    private val changePhoneNumberLiveDataObserver = Observer<String> { value ->
        value?.let { phoneNumberTextView.text = it }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val monitorApplication = MonitorApplication.getInstance()

        monitorApplication.loggedInUsername.observe(this, changeUsernameLiveDataObserver)
        monitorApplication.loggedInFirstName.observe(this, changeFirstNameLiveDataObserver)
        monitorApplication.loggedInLastName.observe(this, changeLastNameLiveDataObserver)
        monitorApplication.loggedInEmail.observe(this, changeEmailLiveDataObserver)
        monitorApplication.loggedInPhoneNumber.observe(this, changePhoneNumberLiveDataObserver)
    }
}