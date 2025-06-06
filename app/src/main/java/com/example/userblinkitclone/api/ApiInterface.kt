package com.example.userblinkitclone.api

import com.example.userblinkitclone.models.CheckStatus
import okhttp3.Headers
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface ApiInterface {

    @GET("apis/pg-sandbox/pg/v1/status/{merchantId}/{transactionId}")
    suspend fun checkStatus(
        @HeaderMap headers : Map<String, String>,
        @Path("merchantId") merchantId : String,
        @Path("transactionId") transactionId : String,

    ) : Response<CheckStatus>
}