package com.example.endsemprojectjet.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.endsemprojectjet.Bus
import com.example.endsemprojectjet.StopsMeta
import com.example.endsemprojectjet.utils.getCurrentLocation
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties

@Composable
fun MapScreen(
    context: Context,
    StopsMeta: List<StopsMeta>,
    userLocation: LatLng,
    liveBusData: List<Bus>
) {
    var showMap by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var mapProperties by remember { mutableStateOf(MapProperties()) }


    getCurrentLocation(context) {
        location = it
        showMap = true
    }

    if (showMap) {
        MyMap(
            context = context,
            latLng = location,
            StopsMeta = StopsMeta,
            liveBusData = liveBusData,
            userLocation = userLocation,
            mapProperties = mapProperties)
    } else {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CircularProgressIndicator()
        }
    }
}