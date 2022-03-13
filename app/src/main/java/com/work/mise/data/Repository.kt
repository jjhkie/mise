package com.work.mise.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.work.mise.BuildConfig
import com.work.mise.data.model.airquality.MeasuredValue
import com.work.mise.data.model.monitoringstation.MonitoringStation
import com.work.mise.data.services.AirKoreaApiService
import com.work.mise.data.services.kakaoLocalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

//싱글턴 패턴이 적용되어 객체가 한번만 생성되도록 한다.
object Repository {

    //주변 관측소 위치 확인
    suspend fun getNearbyMonitoringStation(longitude: Double, latitude: Double) : MonitoringStation?{
        val tmCoordinates = kakaoLocalApiService
            .getTmCoordinates(longitude,latitude)
            .body()
            ?.documents//배열로 되어있다.
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y


        return airKoreaApiService
            .getNearbyMonitoringStation(tmX!!,tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull { it.tm ?:Double.MAX_VALUE }
    }

    suspend fun getLatestAirQualityData(stationName:String):MeasuredValue?=
        airKoreaApiService
            .getRealtimeAirQualities(stationName)
            .body()
            ?.response
            ?.body
            ?.measuredValues
            ?.firstOrNull()

    private val airKoreaApiService: AirKoreaApiService by lazy{
        val gson: Gson = GsonBuilder().setLenient().create()

        Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))//gson으로 Convert 해야한다.
            .client(buildHttpClient())
            .build()
            .create()
    }
    private val kakaoLocalApiService: kakaoLocalApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())//gson으로 Convert 해야한다.
            .client(buildHttpClient())
            .build()
            .create()
    }

    //logging 하는 코드
    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply{
                    level = if(BuildConfig.DEBUG){
                        HttpLoggingInterceptor.Level.BODY
                    }else{
                        HttpLoggingInterceptor.Level.NONE
                    }

                }
            )
            .build()
}