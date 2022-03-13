package com.work.mise.data.services

import com.work.mise.BuildConfig
import com.work.mise.data.model.tmcoordinates.TmCoordinatestResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface  kakaoLocalApiService {

//카카오 좌표계 받기
    //일시정지가 가능한 코드(suspend fun)
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")//인증정보를 넘겨준다.
    @GET("/v2/local/geo/transcoord.json?output_coord=TM")//json으로 받을 거고 output 정보는 tm으러 받는다.
    suspend fun getTmCoordinates(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ): Response<TmCoordinatestResponse>//api 서비스에 리턴한다.
}
