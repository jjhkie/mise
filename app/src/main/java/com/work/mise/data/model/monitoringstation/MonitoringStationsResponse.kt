package com.work.mise.data.model.monitoringstation


import com.google.gson.annotations.SerializedName

data class MonitoringStationsResponse(
    @SerializedName("response")
    val response: Response?
)