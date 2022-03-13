package com.work.mise.data.services

import com.work.mise.BuildConfig
import com.work.mise.data.model.monitoringstation.MonitoringStationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApiService {

    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList"+
    "?serviceKey=${BuildConfig.AIRKOREA_SERVICE_KEY}"+ "&returnType=json")
    suspend fun getNearbyMonitoringStation(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>
}