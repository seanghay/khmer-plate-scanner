package com.seanghay.khmervehiclescanner

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface HttpService {

    @GET
    fun getInfo(@Url url: String): Single<ResponseBody>

    companion object {

        val retrofit = Retrofit.Builder()
            .baseUrl("http://ts.mpwt.gov.kh/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

        fun get(): HttpService = retrofit.create(HttpService::class.java)
    }
}