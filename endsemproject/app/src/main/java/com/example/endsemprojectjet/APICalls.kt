package com.example.endsemprojectjet

import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
suspend fun fetchRealtimeData(): ResponseBody? {
    return withContext(Dispatchers.IO) {
        try {
            val apiService = RetrofitClient.getClient()
            val response = apiService.getFeed()

            if (response.contentLength() > 0) { // Check content length for non-empty response
                response
            } else {
                null
            }
        } catch (e: HttpException) {
            Log.e("RetrofitFailure", "Failed to make Retrofit call HttpException: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("RetrofitFailure", "Failed to make Retrofit call Exception: ${e.message}", e)
            null
        }
    }
}