package nl.jastrix_en_coeninblix.kindermonitor_app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIHelper {
    var apiService: APIService? = null

    fun returnAPIService() : APIService {
        if (apiService == null){
            val retrofit = Retrofit.Builder()
                .baseUrl("https://ehealthapi-fun.azurewebsites.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(APIService::class.java)
        }

        return apiService!!
    }
}