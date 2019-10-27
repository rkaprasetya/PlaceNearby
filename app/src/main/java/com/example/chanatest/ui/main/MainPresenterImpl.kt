package com.example.chanatest.ui.main

import android.location.Location
import android.util.Log
import com.example.chanatest.model.LocationCompact
import com.example.chanatest.model.Place
import com.example.chanatest.model.ResultsItem
import com.example.chanatest.network.ApiClient
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.collections.ArrayList

class MainPresenterImpl(var view : MainContracts.view?):
    MainContracts.presenter {

    lateinit var call: Observable<Place>
    lateinit var adapter: MainAdapter
    /**
     * Check userpermission to get current location
     */
    override fun getSelfLocation() {
        Log.e("getSelfLocation","getSelfLocation")
        when(view?.checkPermissions()){
            false -> view?.requestPermission()
            true -> view?.getLastLocation { callback ->
                if(callback != null) {
                    loadPlaceData(callback,view?.getCategory()!!.toLowerCase())
                }
            }
        }
    }

    /**
     * Retrieve data from server using retrofit and rxjava
     */
    override fun loadPlaceData(location: Location?,category : String){
        Log.e("loadPlaceData","loadPlaceData")
        val loc = location?.latitude.toString()+","+location?.longitude.toString()
        call = ApiClient().apiPlace.postRequest(loc,"1500",category,
            api_key
        )
        call.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Place> {
                override fun onComplete() {
                    Log.d(TAG,"onComplete")
                    view!!.counter++
                }

                override fun onSubscribe(d: Disposable) {
                    Log.d(TAG,"onSubscribe")
                    view?.showProgressBar()
                }

                override fun onNext(data: Place) {
                    setAdapter(data.results,location!!)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG,"message = ${e.message}")
                    view?.showToast("Data cannot be retrieved now. Please try again later.")
                    view?.hideProgressBar()
                }
            })
    }

    private fun setAdapter(data: List<ResultsItem?>?, currentLocation: Location){
        val listDistance = setNewList(data,currentLocation)
        adapter = MainAdapter(listDistance)
        view?.setAdapter(adapter)

    }

    /**
     * Create a new list which only consist the distance and others data that will be shown on the list
     * the list is sorted by distance ASC
     */
    private fun setNewList(originList: List<ResultsItem?>?,location: Location):ArrayList<LocationCompact>{
        var newList = ArrayList<LocationCompact>()

        for(i in originList!!){
            var status = "Unavailable"
            var rating = "Unavailable"

            if(i?.openingHours != null) status = setStatus(i)
            if(i?.rating != null) rating = String.format("%.1f", i?.rating)

            val mDistance = LocationCompact(i?.name.toString()
                ,rating
                ,status
            ,getDistance(location,i?.geometry?.location?.lat,i?.geometry?.location?.lng))
            newList.add(mDistance)
        }
        newList = ArrayList(newList.sortedBy { it.dist })
        return newList
    }

    private fun setStatus(item:ResultsItem?)= when (item?.openingHours?.openNow!!) {
                true -> "Hours : Open Now"
                else -> "Hours : Closed"
            }


    /**
     * Calculate the distance between the user and each place
     */
    private fun getDistance(location : Location,destLat : Double?, destLang : Double?):Double{
        val dist = FloatArray(1)
        Location.distanceBetween(location.latitude,location.longitude,destLat!!,destLang!!,dist)
        return dist[0] * 0.000621371192f.toDouble()
    }
    override fun onDestroy() {
        view = null
    }


    companion object{
        const val api_key = "AIzaSyCh6l-TyWv3b8vc5N2Le88Z0do8IfNTFuk"
        const val TAG = "MainPresenterImpl"
    }
}