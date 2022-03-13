package com.work.mise.data.services

import com.work.mise.BuildConfig
import com.work.mise.data.model.airquality.airQualityResponse
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

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty"
    +"?serviceKey=${BuildConfig.AIRKOREA_SERVICE_KEY}"
    +"&returnType=json"
    +"&dataTerm=DAILY"
    +"&ver=1.3")
    suspend fun getRealtimeAirQualities(
        @Query("stationName") stationName:String

    ):Response<airQualityResponse>
}