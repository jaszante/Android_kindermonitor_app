package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

import nl.jastrix_en_coeninblix.kindermonitor_app.enums.SensorType

class PatientSensor(var sensorID: Int, var sensorType: SensorType, var thresholdMin: Int, var thresholdMax: Int) {
//    var sensorID: Int
//    var sensorType: SensorType
//
//    init {
//        this.sensorID = sensorID
//        this.sensorType = sensorType
//    }
}