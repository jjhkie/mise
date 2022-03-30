package com.work.mise.data.services

import com.work.mise.BuildConfig
import com.work.mise.data.model.weathers.WeathersResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst"
    +"?serviceKey=${BuildConfig.WEATHER_API_KEY}"
    +"&dataType=json")
    fun doGetJsonDataWeather(
        @Query("base_date") base_date: String,
        @Query("base_time") base_time: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): Call<WeathersResponse>

}
