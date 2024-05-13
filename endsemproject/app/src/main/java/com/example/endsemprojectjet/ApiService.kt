package com.example.endsemprojectjet


import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("realtime/VehiclePositions.pb")
    suspend fun getFeed(
        @Query("key") key: String = "tvKJQxkw13wVPHGWNwgK0L75xQxsec01"
    ): ResponseBody
}