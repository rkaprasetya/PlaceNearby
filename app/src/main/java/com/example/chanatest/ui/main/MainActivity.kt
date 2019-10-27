package com.example.chanatest.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chanatest.R
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), MainContracts.view, View.OnClickListener {

    override var counter: Int = 0
    private var presenter = MainPresenterImpl(this)
    private var fusedLocationClient: FusedLocationProviderClient? = null
    lateinit var locationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        init()
        checkGps()
    }

    override fun getCategory(): String {
        return spinner_main.selectedItem.toString().toLowerCase(Locale.ENGLISH)
    }

    private fun showCategory() {
        spinner_main.visibility = View.VISIBLE
    }

    override fun setAdapter(adapter: MainAdapter) {
        rv_place.adapter = adapter
        adapter.notifyDataSetChanged()
        showCategory()
        hideProgressBar()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_refresh -> presenter.getSelfLocation()
        }
    }

    private fun init() {
        locationCallback = LocationCallback()
        rv_place.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        btn_refresh.setOnClickListener(this)
        setSpinnerItemSelectedListener()
    }

    override fun showProgressBar() {
        ll_progress.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        ll_progress.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            presenter.getSelfLocation()
        } else if (requestCode == 1) {
            checkGps()
        }
    }

    private fun setSpinnerItemSelectedListener() {
        spinner_main.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selection = parent?.getItemAtPosition(position)
                (parent?.getChildAt(0) as TextView).setTextColor(
                    resources.getColor(
                        R.color.white,
                        null
                    )
                )
                getLastLocation { callback ->
                    if (callback != null && counter != 1)
                        presenter.loadPlaceData(
                            callback,
                            selection.toString().toLowerCase(Locale.ENGLISH)
                        )

                    counter++
                }

            }

        }
    }


    override fun checkPermissions(): Boolean {
        val permissionState =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        if (shouldProvideRationale) {
            showPopUp(
                resources.getString(R.string.permission_require),
                Settings.ACTION_APPLICATION_SETTINGS,
                0
            )
        } else {
            startLocationPermissionRequest()
        }
    }

    /**
     * Get last location. if there is no last location data in cache,
     * it will request location update
     */
    override fun getLastLocation(callback: (Location?) -> Unit) {
        var lastLoc: Location?
        getFusedLocationClient()!!.lastLocation.addOnCompleteListener(this) { task ->
            if (task?.result == null) {

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult != null && locationResult.locations.isNotEmpty()) {
                            val newLocation = locationResult.locations[0]
                            callback.invoke(newLocation)

                        } else {
                            callback.invoke(null)
                            showToast(resources.getString(R.string.no_location))
                        }
                    }
                }

                val locationRequest = getLocationRequest()

                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
            } else if (task.isSuccessful && task.result != null) {
                lastLoc = task.result
                callback.invoke(lastLoc)
            }

        }

    }

    private fun getLocationRequest() = LocationRequest().apply {
        fastestInterval = 5000
        interval = 10000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getFusedLocationClient(): FusedLocationProviderClient? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        return fusedLocationClient
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf((Manifest.permission.ACCESS_COARSE_LOCATION)),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun getSpinnerItem() = spinner_main.selectedItem.toString()


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation { callback ->
                    if (callback != null)
                        presenter.loadPlaceData(callback, getSpinnerItem())
                }
            } else {
                showPopUp(
                    resources.getString(R.string.permission_require),
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    0
                )
            }
        }
    }

    private fun showPopUp(message: String, mode: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.warning))
        builder.setMessage(message)
        builder.setPositiveButton("Settings") { dialog, which ->
            startActivityForResult(Intent(mode), requestCode)
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
            finish()
        }
        builder.show()

    }

    /**
     * Check if location service is enable or not.
     * If it is not enabled, it will prompt a popup that asks user to turn it on.
     */
    override fun checkGps() {
        val isGpsEnable: Boolean
        val isNetworkEnable: Boolean
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnable && !isNetworkEnable) {
            showPopUp(
                resources.getString(R.string.gps_service),
                Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                1
            )
        } else {
            presenter.getSelfLocation()
        }

    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }
}
