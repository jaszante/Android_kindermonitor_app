package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

import java.util.*

data class Measurement (
    val measurementID: Int,
    val sensorID: Int,
    val value: Double,
    val time: String
)