package com.example.endsemprojectjet.ui


import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.endsemprojectjet.Bus
import com.example.endsemprojectjet.R
import com.example.endsemprojectjet.StopsMeta
import com.example.endsemprojectjet.utils.bitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun MyMap(
    context: Context,
    latLng: LatLng,
    StopsMeta: List<StopsMeta>,
    liveBusData: List<Bus>,
    userLocation: LatLng,
    mapProperties: MapProperties = MapProperties(),
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = {

            }
        ) {
            StopsMeta.forEach { stop ->
                Marker(
                    state = MarkerState(position = stop.location),
                    title = stop.stop_name,
                    snippet = stop.stop_code,
                    icon = null
                )
            }
            println("User Location -----------------")
            println(userLocation)
            Marker(
                state = MarkerState(position = userLocation),
                title = "User",
                snippet = "You are here",
                icon = bitmapDescriptor(context, R.drawable.user)
            )
            liveBusData.forEach { bus ->
                Marker(
                    state = MarkerState(position = LatLng(bus.latitude,bus.longitude)),
                    title = bus.destination,
                    snippet = "Next Stop: "+bus.nextStop,
                    icon = bitmapDescriptor(context, R.drawable.bus)
                )
            }

        }
    }
}