package nl.jastrix_en_coeninblix.kindermonitor_app.api

import android.content.Intent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.mainAcitivityContext
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.observableToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.AuthenticationToken
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

    fun returnAPIServiceWithAuthenticationTokenAdded() : APIService {
        val token = observableToken.authToken
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

    // only called if observableToken has expired
//    fun loginWithCachedUsernameAndPassword() {
//        if (!currentlyExecutingLoginCall) {
//            currentlyExecutingLoginCall = true
//            if (userName != null && password != null && userName != "" && password != "") {
//
//                val call = apiService!!.userLogin(UserLogin(userName, password))
//
//                call.enqueue(object : Callback<AuthenticationToken> {
//                    override fun onResponse(call: Call<AuthenticationToken>, response: retrofit2.Response<AuthenticationToken>) {
//                        if (response.isSuccessful && response.body() != null){
//                            currentlyExecutingLoginCall = false
//                            val newToken = response.body()!!.token
//                            buildAPIServiceWithNewToken(newToken) // important that we build the apiservice again with new token before the observabletoken is changed
//                            observableToken.changeToken(newToken)
//                        }
//                        else {
//                            logOutUserAndStartLoginActivity()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<AuthenticationToken>, t: Throwable) {
//                        logOutUserAndStartLoginActivity()
//                    }
//
//                    private fun logOutUserAndStartLoginActivity() {
//                        currentlyExecutingLoginCall = false
//
//                        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//
//                        val sharedPreferences = EncryptedSharedPreferences.create(
//                            "kinderMonitorApp",
//                            masterKeyAlias,
//                            mainAcitivityContext,
//                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//                        )
//
//                        val editor = sharedPreferences.edit()
////        val editor = getSharedPreferences("kinderMonitorApp", Context.MODE_PRIVATE).edit()
//                        editor.putString("AuthenticationToken", null)
//                        editor.putString("KinderMonitorAppUserName", null)
//                        editor.putString("KinderMonitorAppPassword", null)
//                        editor.apply()
//
//                        val intent: Intent = Intent(mainAcitivityContext, LoginActivity::class.java)
//                        mainAcitivityContext.startActivity(intent)
//                    }
//                })
//            }
//        }
//    }

    fun buildAPIServiceWithNewToken(newToken: String): APIService {
        val client = OkHttpClient.Builder().addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
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
        return apiService!!
    }
}