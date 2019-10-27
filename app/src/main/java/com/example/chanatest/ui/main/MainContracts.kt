package com.example.chanatest.ui.main

import android.location.Location
import com.example.chanatest.base.BasePresenter

interface MainContracts {
    interface view{
        fun checkPermissions(): Boolean
        fun requestPermission()
        fun getLastLocation(callback: (Location?) ->Unit)
        fun getCategory():String
        fun checkGps()
        fun setAdapter(adapter: MainAdapter)
        fun showProgressBar()
        fun hideProgressBar()
        fun showToast(message:String)
        var counter : Int
    }
    interface presenter: BasePresenter{
        fun getSelfLocation()
        fun loadPlaceData(location: Location?, category:String)
    }
}