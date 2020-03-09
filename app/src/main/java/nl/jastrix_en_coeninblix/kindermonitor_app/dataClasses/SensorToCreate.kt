package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class SensorToCreate (
    val Type: String,
    val Brand: String,
    val ThresholdMin: String,
    val ThresholdMax: String,
    val PushnotificationDeviceToken:String
)