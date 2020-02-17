package nl.jastrix_en_coeninblix.kindermonitor_app.api

//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.authToken
import nl.jastrix_en_coeninblix.kindermonitor_app.MonitorApplication
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.password
//import nl.jastrix_en_coeninblix.kindermonitor_app.MainActivity.Companion.userName
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
                .baseUrl("https://kindermonitoringapi.azurewebsites.net/api/") //"https://ehealthapi-fun.azurewebsites.net/api/") //"https://ehealthapi-fun.azurewebsites.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(APIService::class.java)
    }

    fun returnAPIServiceWithAuthenticationTokenAdded() : APIService {
        if (apiService == null) {
            val token = MonitorApplication.getInstance().authToken
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