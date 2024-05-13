package com.example.endsemprojectjet

import com.google.android.gms.maps.model.LatLng


data class Bus(
    val entity_id: String,
    val trip_id: String,
    var start_time: String,
    var start_date: String,
    var route_id: String,
    var latitude: Double,
    var longitude: Double,
    var vehicle_id: String,
    var vehicle_name: String,
    var bus_distance: Double,
    var bus_stop_distance: Double,
    var next_km: Double,
    var nextStop: String,
    var source: String,
    var destination: String,
    var isCompleted: Boolean,
    var stops_remaning: Int,
    var stop_list: List<Stops>
)
data class Stops(
    var stop_id: String,
    var stop_name: String,
    var lat: Double,
    var lon: Double,
    var stop_code: String,
    var isVisited: Boolean,
    var stop_distance: Double,
    var isNextStop: Boolean
)
data class StopsMeta(
    var stop_id: String,
    var stop_name: String,
    var location: LatLng,
    var stop_code: String,
)