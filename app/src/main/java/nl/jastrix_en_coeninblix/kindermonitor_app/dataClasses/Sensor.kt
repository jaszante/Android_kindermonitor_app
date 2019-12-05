package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class Sensor (
    val SensorId: Int,
    val PatientId: Int,
    val Type: String,
    val Brand: String,
    val ThresholdMin: String,
    val ThresholdMax: String,
    val PreSharedKey: String,
    val InUseSince: String
)