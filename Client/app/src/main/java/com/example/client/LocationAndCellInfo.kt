
package com.example.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.telephony.*
import android.util.Log

class LocationAndCellInfo(private val context: Context) {

    companion object {
        private const val TAG = "LocationAndCellInfo"
    }

    // Declare the LocationManager for location services
    private lateinit var locationManager: LocationManager
    // Declare the TelephonyManager for telephony services
    private lateinit var telephonyManager: TelephonyManager

    // Define a LocationListener to handle location updates
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Log location changes
            Log.i(TAG, "Location changed...")
            Log.i(TAG, "Latitude : ${location.latitude}")
            Log.i(TAG, "Longitude : ${location.longitude}")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    // Initialize the LocationManager and TelephonyManager
    fun initialize() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    // Start location updates
    fun startLocationUpdates() {
        val provider = LocationManager.GPS_PROVIDER
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission not granted")
            return
        }
        // Request location updates from the location manager
        locationManager.requestLocationUpdates(provider, 1000, 1f, locationListener)
    }

    // Stop location updates
    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    // Get the last known location
    fun getLastKnownLocation(): String {
        val provider = LocationManager.GPS_PROVIDER
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "بدون مجوز + bbb" // "Without permission" in Persian
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(provider)
        // Return location details if available
        return if (lastKnownLocation != null) {
            val lat = lastKnownLocation.latitude
            val lon = lastKnownLocation.longitude
            val alt = lastKnownLocation.altitude
            val acc = lastKnownLocation.accuracy
            val spd = lastKnownLocation.speed
            "عرض جغرافیایی: $lat\nطول جغرافیایی: $lon\nارتفاع: $alt\nدقت: $acc\nسرعت: $spd" // Location details in Persian
        } else {
            "مکان نامشخص" // "Unknown location" in Persian
        }
    }

    // Get cell information
    fun getCellInfo(): String {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "بدون مجوز") // "Without permission" in Persian
            return "بدون مجوز + hhhh" // "Without permission" in Persian
        }
        val cellInfoList = telephonyManager.allCellInfo
        val cellInfoStr = StringBuilder()
        // Iterate through the list of cell information
        for (cellInfo in cellInfoList) {
            when (cellInfo) {
                is CellInfoGsm -> {
                    val cellIdentityGsm = cellInfo.cellIdentity
                    val cellSignalStrengthGsm = cellInfo.cellSignalStrength
                    val info = "0:${cellIdentityGsm.cid},${cellSignalStrengthGsm.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoCdma -> {
                    val cellIdentityCdma = cellInfo.cellIdentity
                    val cellSignalStrengthCdma = cellInfo.cellSignalStrength
                    val info = "1:${cellIdentityCdma.basestationId},${cellSignalStrengthCdma.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoLte -> {
                    val cellIdentityLte = cellInfo.cellIdentity
                    val cellSignalStrengthLte = cellInfo.cellSignalStrength
                    val info = "2:${cellIdentityLte.ci},${cellSignalStrengthLte.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                is CellInfoWcdma -> {
                    val cellIdentityWcdma = cellInfo.cellIdentity
                    val cellSignalStrengthWcdma = cellInfo.cellSignalStrength
                    val info = "3:${cellIdentityWcdma.cid},${cellSignalStrengthWcdma.dbm};"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
                else -> {
                    val info = "Unknown Cell Type\n"
                    cellInfoStr.append(info)
                    Log.i(TAG, info)
                }
            }
        }
        return cellInfoStr.toString()
    }
}