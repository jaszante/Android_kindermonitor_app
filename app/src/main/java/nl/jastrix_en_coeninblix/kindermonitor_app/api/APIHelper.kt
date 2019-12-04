package nl.jastrix_en_coeninblix.kindermonitor_app.api

import android.content.Context
import android.content.Intent
import android.util.Log
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.mainActivity
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserData
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.UserLogin
import nl.jastrix_en_coeninblix.kindermonitor_app.login.LoginActivity
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import java.io.IOException


class APIHelper {
    private var apiService: APIService? = null
    private var currentlyExecutingLoginCall = false

    fun buildAndReturnAPIService(): APIService {
            val retrofit = Retrofit.Builder()
                .baseUrl( "https://ehealthapi-fun.azurewebsites.net/api/") //"https://ehealthapi-fun.azurewebsites.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(APIService::class.java)
    }

    fun returnAPIServiceWithAuthenticationTokenAdded(token : String) : APIService {
        if (apiService == null) {
            val client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    return chain.proceed(newRequest)
                }
            }).build()

            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl("https://ehealthapi-fun.azurewebsites.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(APIService::class.java)
        }

        return apiService!!
    }

    // only called if token has expired
    fun loginWithCachedUsernameAndPassword() {
        if (!currentlyExecutingLoginCall) {
            currentlyExecutingLoginCall = true
            if (userName != null && password != null && userName != "" && password != "") {

                val call = apiService!!.userLogin(UserLogin(userName, password))

                call.enqueue(object : Callback<AuthenticationToken> {
                    override fun onResponse(call: Call<AuthenticationToken>, response: retrofit2.Response<AuthenticationToken>) {
                        if (response.isSuccessful && response.body() != null){
                            currentlyExecutingLoginCall = false
                            buildAPIServiceWithNewToken(response.body()!!.token)
                            mainActivity.initDrawerWithUserInformation()
                        }
                        else {
                            logOutUserAndStartLoginAcitivy()
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
                        logOutUserAndStartLoginAcitivy()
                    }

                    private fun logOutUserAndStartLoginAcitivy() {
                        currentlyExecutingLoginCall = false
                        mainActivity.startLoginAcitivity()
                    }
                })
            }
        }
    }

    private fun buildAPIServiceWithNewToken(token: String) {
        val client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(newRequest)
            }
        }).build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://ehealthapi-fun.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(APIService::class.java)
    }
}