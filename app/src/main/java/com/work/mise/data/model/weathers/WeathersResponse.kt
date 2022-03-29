package com.work.mise.data.model.weathers


import com.google.gson.annotations.SerializedName

data class WeathersResponse(
    @SerializedName("response")
    val response: Response?
)