package com.example.chanatest.network

import com.example.chanatest.model.Place
import io.reactivex.Observable
import retrofit2.http.*

interface ApiService {
   // https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyCh6l-TyWv3b8vc5N2Le88Z0do8IfNTFuk

    @GET("json?")
    fun postRequest(@Query("location") location:String,
                    @Query("radius") radius:String,
                    @Query("type") type:String,
                    @Query("key") key:String): Observable<Place>
    //@FormUrlEncoded
    @GET("json?{path}")
    fun postReq(@Path("path") path:String):Observable<Place>

//    @GET("cars.json")
//    fun getCarsList(): Observable<List<CarList>>

}