package nl.jastrix_en_coeninblix.kindermonitor_app.api

import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.*
import retrofit2.Call
import retrofit2.http.*

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("Users")
    fun userRegister(@Body userLogin: UserRegister): Call<AuthenticationToken>

    @POST("Users/login")
    fun userLogin(@Body userLogin: UserLogin): Call<AuthenticationToken>

    @GET ("Users/me")
    fun getCurrentUser() : Call<UserData>

    @GET ("Patients/{PatientId}/Sensors")
    fun getPatientsSensors(@Path("PatientId") patientId: String) : Call<Array<SensorFromCallback>>

    @POST ("Patients/{PatientId}/Sensors")
    fun createSensor(@Path("PatientId") patientId: Int, @Body newSensor: SensorToCreate): Call<Sensor>

    @POST ("Users/me/patients")
    fun createPatientForLoggedInUser(@Body patient: Patient): Call<PatientWithID>

    @GET ("Users/me/patients")
    fun getAllPatientsForLogginedInUser(): Call<Array<PatientWithID>>

    @GET ("Sensors/{SensorId}/measurements")
    fun getMeasurementsForSensor(@Path("SensorId") sensorId: Int): Call<Array<Measurement>> //, from: String? = null, to: String? = null

    @GET ("Sensors/{SensorId}/measurements")
    fun getMeasurementsForSensorWithRange(@Path("SensorId") sensorId: Int, @Query("from")from : String, @Query("to") to :String ): Call<Array<Measurement>>


////    @POST ("Sensors/{SensorId}/measurements")
//    @POST ("Sensors/measurements")
//    fun postMeasurementToSensor(@Body measurementForPost: MeasurementForPost): Call<String>

    @PUT ("Users/me")
    fun updateCurrentUser(@Body newUserData: UserRegister): Call<Void>
}