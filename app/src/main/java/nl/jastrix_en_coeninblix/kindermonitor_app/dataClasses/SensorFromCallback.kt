package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class SensorFromCallback (
    val sensorID: Int,
    val patientID: Int,
    val type: String,
    val brand: String,
    val thresholdMin: Int,
    val thresholdMax: Int,
    val preSharedKey: String,
    val inUseSince: String,
    val pushnotificationDeviceToken: String
)