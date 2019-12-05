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
    fun getCurrentUser() : Call<UserData> // @Header("Authorization") Authorization : String

    @POST ("Patients/{PatientId}//Sensors")
    fun createSensor(@Path("PatientId") patientId: String)

    @GET ("Patients/{PatientId}//Sensors")
    fun getPatientsSensors(@Path("PatientId") patientId: String) : Call<Array<Sensor>>

    @POST ("Users/me/patients")
    fun createPatientForLoggedInUser(@Body patient: Patient): Call<Patient>

    @GET ("Users/me/patients")
    fun getAllPatientsForLogginedInUser(): Call<Patient>
}