package com.work.mise

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.work.mise.data.Repository
import com.work.mise.data.Url
import com.work.mise.data.model.airquality.Grade
import com.work.mise.data.model.airquality.MeasuredValue
import com.work.mise.data.model.monitoringstation.MonitoringStation
import com.work.mise.data.model.weathers.WeathersResponse
import com.work.mise.data.services.WeatherAPIService
import com.work.mise.databinding.ActivityMainBinding

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val scope = MainScope()



    //TODO 삭제 예정
    val base_time = 1700
    val base_date = 20220329
    val lon = "139"
    val openap = "b4da17e419f46ae14f73118872b2a31d"
    //logging 하는 코드
    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }

                }
            )
            .build()
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //현재 날짜 시간 구하기
        val current: Long = System.currentTimeMillis()

        val time = SimpleDateFormat("HHmm")
        val today = SimpleDateFormat()

        bindViews()
        initVariables()
        requestLocationPermissions()




    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //location 접근 권한이 허가되었는지 확인하는 코드
        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED


        val backgroundLocationPermissionGranted =
            requestCode == REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!backgroundLocationPermissionGranted) {
                requestBackgroundLocationPermissions()
            } else {
                fetchAirQualityData()
            }

        } else {

            if (!locationPermissionGranted) {
                finish()
            } else {
                //권한이 있을 경우
                //fetchData
                fetchAirQualityData()
            }
        }
    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            fetchAirQualityData()
        }
    }

    //초기화
    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }


    //권한 요청
    //권한이 없을 경우 권한을 설정하는 메시지를 띄운다
    //manifest에 요청한 권한 2개
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {
        cancellationTokenSource = CancellationTokenSource()

        //getCurrentLocation (int priority, CancellationToken token)
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            //location 정보를 받아온 걸 성공하였으며
            //실제로 api 를 호출한 부분이므로 launch로 시작한다.
            Log.d("weather", location.toString())
            Log.d("weather", location.toString())

            scope.launch {
                binding.errorDescriptionTextView.visibility = View.GONE
                try {
                    val monitoringStation =
                        Repository.getNearbyMonitoringStation(location.longitude, location.latitude)

                    val measuredValue =
                        Repository.getLatestAirQualityData(monitoringStation!!.stationName!!)

                    val tmp : LatXLngY =convertGRID_GPS(TO_GRID,location.latitude,location.longitude)
                    Log.d("weather", "변환값 "+tmp.x.toInt())
                    Log.d("weather", "변환값 "+tmp.y.toInt())


                    val retrofit = Retrofit.Builder()
                        .baseUrl(Url.COMMON_API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(buildHttpClient())
                        .build()

                    val weatherService = retrofit.create(WeatherAPIService::class.java)

                    val cu_weather = weatherService.doGetJsonDataWeather(base_date,base_time,tmp.x.toInt(),tmp.y.toInt())
                    //val open_weather = weatherService.doGetJsonDataWeather(location.longitude,location.latitude,openap)

                    //val wea :JSONObject = JSONObject(items)
                    //Log.d("weather","1"+open_weather.toString())
                    Log.d("weather","2"+measuredValue.toString())

                    cu_weather.enqueue(object:retrofit2.Callback<WeathersResponse>{
                        override fun onResponse(
                            call: Call<WeathersResponse>,
                            response: Response<WeathersResponse>
                        ) {
                            if(response.isSuccessful){
                                Log.d("weather","이거는" + response.body())
                                response.body()?.let{
                                    it.response!!.body!!.items!!.item!!.forEach{
                                        Log.d("weather", it.toString())
                                        when(it.category ){
                                            "TMP" -> binding.categoryTmpText.text = it.fcstValue +" ℃"
                                            "POP" -> binding.rainPOPText.text = it.fcstValue+"%"
                                            "SKY" ->if (it.fcstValue!!.toInt() < 6){
                                                if(base_time>1600){
                                                    binding.skyStateImage.setBackgroundResource(R.drawable.sunny_icon)
                                                    binding.skyStateText.text = "The sky is clear"
                                                }else
                                                binding.skyStateText.text = "The sky is clear"
                                                binding.skyStateImage.setBackgroundResource(R.drawable.cloudy_sun_icon)
                                            }else if(it.fcstValue!!.toInt() < 9 ){
                                                binding.skyStateText.text = "mostly cloudy"
                                                binding.skyStateImage.setBackgroundResource(R.drawable.cloudy_sun_icon)
                                            }else{
                                                binding.skyStateText.text = "The sky has become overcast."
                                                binding.skyStateImage.setBackgroundResource(R.drawable.clouds_weather)
                                            }
                                            "WSD" ->binding.windWSDText.text = it.fcstValue +"m/s"
                                        }
                                    }
                                }
                            }else{
                                Log.d("weather","실패야..")
                            }
                        }

                        override fun onFailure(call: Call<WeathersResponse>, t: Throwable) {
                            Log.d("weather","실패했습니다")
                        }

                    })


                    displayAirQualityData(monitoringStation, measuredValue!!)
                } catch (exception: Exception) {
                    binding.errorDescriptionTextView.visibility = View.VISIBLE
                    binding.contentLayout.alpha = 0F
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.refresh.isRefreshing = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun displayAirQualityData(monitoringStation: MonitoringStation, measuredValue: MeasuredValue) {
        binding.contentLayout.animate()
            .alpha(1F)
            .start()

        binding.measuringStationNameTextView.text = monitoringStation.stationName
        binding.measuringStationAddressTextView.text = monitoringStation.addr

        (measuredValue.khaiGrade ?: Grade.UNKNOWN).let { grade ->
            binding.root.setBackgroundResource(grade.colorResId)
            binding.totalGradeLabelTextView.text = grade.label
            binding.totalGradeEmojiTextView.text = grade.emoji
        }
        with(measuredValue) {
            binding.fineDustInformationTextView.text =
                " $pm10Value ㎍/㎥ "
            binding.ultraFineDustInformationTextView.text =
                " $pm25Value ㎍/㎥ "

            with(binding.so2Item) {
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }
            with(binding.coItem) {
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$coValue ppm"
            }
            with(binding.o3Item) {
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$o3Value ppm"
            }
            with(binding.no2Item) {
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$no2Value ppm"
            }
        }
    }


    //좌표 변환 코드
    private fun convertGRID_GPS(mode: Int, lat_X: Double, lng_Y: Double): LatXLngY {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0 // 격자 간격(km)
        val SLAT1 = 30.0 // 투영 위도1(degree)
        val SLAT2 = 60.0 // 투영 위도2(degree)
        val OLON = 126.0 // 기준점 경도(degree)
        val OLAT = 38.0 // 기준점 위도(degree)
        val XO = 43.0 // 기준점 X좌표(GRID)
        val YO = 136.0 // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //
        val DEGRAD = Math.PI / 180.0
        val RADDEG = 180.0 / Math.PI
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD
        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)
        val rs: LatXLngY = LatXLngY()
        if (mode == TO_GRID) {
            rs.lat = lat_X
            rs.lng = lng_Y
            var ra = Math.tan(Math.PI * 0.25 + lat_X * DEGRAD * 0.5)
            ra = re * sf / Math.pow(ra, sn)
            var theta = lng_Y * DEGRAD - olon
            if (theta > Math.PI) theta -= 2.0 * Math.PI
            if (theta < -Math.PI) theta += 2.0 * Math.PI
            theta *= sn
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5)
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5)
        } else {
            rs.x = lat_X
            rs.y = lng_Y
            val xn = lat_X - XO
            val yn = ro - lng_Y + YO
            var ra = Math.sqrt(xn * xn + yn * yn)
            if (sn < 0.0) {
                ra = -ra
            }
            var alat = Math.pow(re * sf / ra, 1.0 / sn)
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5
            var theta = 0.0
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0
            } else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5
                    if (xn < 0.0) {
                        theta = -theta
                    }
                } else theta = Math.atan2(xn, yn)
            }
            val alon = theta / sn + olon
            rs.lat = alat * RADDEG
            rs.lng = alon * RADDEG
        }
        return rs
    }

    internal inner class LatXLngY {
        var lat = 0.0
        var lng = 0.0
        var x = 0.0
        var y = 0.0
    }

    companion object {
        var TO_GRID = 0
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
        private const val REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 101
    }


}