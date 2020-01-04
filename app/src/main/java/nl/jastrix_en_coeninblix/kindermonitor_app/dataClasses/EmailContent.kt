package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

class EmailContent(sensorID: String, preSharedKey: String, sensorType: String) {
    var sensorID: String
    var preSharedKey: String
    var sensorType: String

    init {
        this.sensorID = sensorID
        this.preSharedKey = preSharedKey
        this.sensorType = sensorType
    }
}