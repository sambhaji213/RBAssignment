package com.sk.rbassignment.retrofit

import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.MINUTES

object ServiceGenerator {

    private val builder = Builder()
        .baseUrl(ApplicationConstant.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

    fun <S> callAPICreateService(serviceClass: Class<S>?): S {
        return aPIRequest.create(serviceClass)
    }

    private val aPIRequest: Retrofit
        get() {
            val httpClient = OkHttpClient.Builder()
                .readTimeout(ApplicationConstant.TIMEOUT_READ.toLong(), MINUTES)
                .connectTimeout(ApplicationConstant.TIMEOUT_CONNECTION.toLong(), MINUTES)
            httpClient.addInterceptor { chain: Chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = BODY
            httpClient.addInterceptor(interceptor)
            val client = httpClient.build()
            return builder.client(client).build()
        }
}