package nl.jastrix_en_coeninblix.kindermonitor_app.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
import nl.jastrix_en_coeninblix.kindermonitor_app.R
import nl.jastrix_en_coeninblix.kindermonitor_app.graphPage.GraphPage
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProviders.of(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        galleryViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logoutButton = getView()!!.findViewById<Button>(R.id.LogoutButton)
        logoutButton.setOnClickListener() {
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
        }


        val graphbutton = getView()!!.findViewById<Button>(R.id.BTNgraph)
        graphbutton.setOnClickListener() {
            val intent = Intent(activity, GraphPage::class.java)
            startActivity(intent)

        }

    }
}