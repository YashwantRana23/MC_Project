package com.example.endsemprojectjet


import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://otd.delhi.gov.in/api/"

    fun getClient(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ProtoConverterFactory.create()) // For protocol buffers
            .build()

        return retrofit.create(ApiService::class.java)
    }
}