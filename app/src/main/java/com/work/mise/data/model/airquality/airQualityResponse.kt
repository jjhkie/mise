package com.work.mise.data.model.airquality


import com.google.gson.annotations.SerializedName

data class airQualityResponse(
    @SerializedName("response")
    val response: Response?
)