package com.work.mise

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.work.mise.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

        if (!locationPermissionGranted) {
            finish()
        } else {
            //권한이 있을 경우
            //fetchData
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
            scope.launch {

            }
        }
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}