package nl.jastrix_en_coeninblix.kindermonitor_app.api

import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


class APIHelper {
    private var apiService: APIService? = null

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
}