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

    @POST ("Sensors/{sensorId}")
    fun updateSensor(@Path("sensorId") patientId: Int, @Body updatedSensor: SensorToCreate): Call<Sensor>

    @POST ("Users/me/patients")
    fun createPatientForLoggedInUser(@Body patient: Patient): Call<PatientWithID>

    @GET ("Users/me/patients")
    fun getAllPatientsForLogginedInUser(): Call<Array<PatientWithID>>

    @GET ("Sensors/{SensorId}/measurements")
    fun getMeasurementsForSensor(@Path("SensorId") sensorId: Int): Call<Array<Measurement>> //, from: String? = null, to: String? = null

    @GET ("Sensors/{SensorId}/measurements")
    fun getMeasurementsForSensorWithRange(@Path("SensorId") sensorId: Int, @Query("from")from : String, @Query("to") to :String ): Call<Array<Measurement>>

    @PUT ("Users/me")
    fun updateCurrentUser(@Body newUserData: UserRegister): Call<Void>

//    @FormUrlEncoded
    @POST ("Users/me/Patients/{patientId}/Permissions")
    fun giveUserPermission(@Path("patientId") patientId: Int, @Body userName: UserObjectWithOnlyUsername): Call<Void>

    @GET ("Users/me/Patients/{patientId}/Permissions")
    fun getAllUsersWithPermission(@Path("patientId") patientId: Int): Call<ArrayList<UserData>>

    @DELETE ("Users/me/Patients/{patientId}/Permissions/{doctorId}")
    fun deleteUserPermission(@Path("patientId") patientId: Int, @Path("doctorId") doctorId: Int): Call<Void>
}