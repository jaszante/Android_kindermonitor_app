package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType

class PatientSensor(sensorID: Int, sensorType: SensorType) {
    var sensorID: Int
    var sensorType: SensorType

    init {
        this.sensorID = sensorID
        this.sensorType = sensorType
    }
}