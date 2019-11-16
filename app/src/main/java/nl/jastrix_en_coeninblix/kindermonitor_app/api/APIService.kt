package nl.jastrix_en_coeninblix.kindermonitor_app.api

import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserRegister
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("Users")
    fun userRegister(@Body userLogin: UserRegister): Call<AuthenticationToken>
//    fun userRegister(@Field("UserName") userName: String,
//                     @Field("Password") password: String,
//                     @Field("FirstName") firstName: String,
//                     @Field("LastName") lastName: String,
//                     @Field("PhoneNumber") phoneNumber: String,
//                     @Field("Email") email: String)


    @POST("Users/login")
    fun userLogin(@Body userLogin: UserLogin): Call<AuthenticationToken>
}