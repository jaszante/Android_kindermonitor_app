package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters

import android.widget.ImageView
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData

interface PermissionListener {
    fun onItemClick(position: Int, patient: UserData)
}