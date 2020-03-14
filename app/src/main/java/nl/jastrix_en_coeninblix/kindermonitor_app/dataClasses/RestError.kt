package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

import com.google.gson.annotations.SerializedName

data class RestError (
    @SerializedName("code")
    var code: Int,
    @SerializedName("error")
    var errorDetails: String
)