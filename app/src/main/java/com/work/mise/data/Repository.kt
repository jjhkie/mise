package com.work.mise.data

import com.work.mise.BuildConfig
import com.work.mise.data.services.kakaoLocalApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

//싱글턴 패턴이 적용되어 객체가 한번만 생성되도록 한다.
object Repository {

    //주변 관측소 위치 확인
    suspend fun getNearbyMonitoringStation(latitude: Double, longitude: Double){
        val tmCoordinates = kakaoLocalApiService
            .getTmCoordinates(latitude,longitude)
            .body()
            ?.documents//배열로 되어있다.
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val txY = tmCoordinates?.y
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