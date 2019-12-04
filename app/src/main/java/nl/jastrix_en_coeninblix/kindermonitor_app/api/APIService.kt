package nl.jastrix_en_coeninblix.kindermonitor_app.api

import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
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
}