package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class Measurement (
    val MeasurementId: Int,
    val SensorId: Int,
    val Value: Int,
    val Time: String
)