package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class Sensor (
    val sensorID: Int,
    val patientID: Int,
    val type: String,
    val brand: String,
    val thresholdMin: String,
    val thresholdMax: String,
    val preSharedKey: String,
    val inUseSince: String
)