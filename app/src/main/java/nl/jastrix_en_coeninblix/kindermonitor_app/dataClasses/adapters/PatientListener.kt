package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.adapters

import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.PatientWithID

interface PatientListener {
    fun onItemClick(position: Int, patient: PatientWithID)
}