package com.sk.rbassignment.retrofit

import com.sk.rbassignment.model.FieldDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("/json")
    fun getFieldsDetails(
        @Query("fields") fields: String?,
    ): Call<FieldDetails>
}
