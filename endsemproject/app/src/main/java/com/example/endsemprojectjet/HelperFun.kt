package com.example.endsemprojectjet

import android.content.Context
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.transit.realtime.GtfsRealtime
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ToggleButton(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange
    )
}


class MyJsonLoader(private val context: Context) {
    fun loadJsonFromFile(filename: String): JSONObject? {
        try {
            val inputStream = context.assets.open(filename)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

fun isInRadius(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean? {
    val distance = calculateDistance(lat1,lon1,lat2,lon2)
    if(distance<=0.10){
        return true
    }
    return false
}
fun deg2rad(deg: Double): Double {
    return deg * (Math.PI / 180)
}
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Radius of the Earth in kilometers

    val dLat1 = Math.toRadians(lat1)
    val dLon1 = Math.toRadians(lon1)
    val dLat2 = Math.toRadians(lat2)
    val dLon2 = Math.toRadians(lon2)
    val dLat = dLat2 - dLat1
    val dLon = dLon2 - dLon1

    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

fun processData(
    liveData: GtfsRealtime.FeedMessage,
    stopDetails: JSONObject?,
    jsonStops: JSONObject?,
    userLat: Double,
    userLon: Double,
    liveBusData: List<Bus>,
    stopList: List<StopsMeta>
): Pair<List<Bus>, List<StopsMeta>> {
    println("Sizeee-----------------")


    val entities = liveData.getEntityList()
    println(entities.size)
    var newStopList = emptyList<StopsMeta>().toMutableList()
    if(entities.size>0){
        var busList = emptyList<Bus>().toMutableList()
        val tripIndexMap = mutableMapOf<String, Int>()
        if (liveBusData.size>0){
            busList = liveBusData.toMutableList()
            liveBusData.forEachIndexed { index, busRow ->
                tripIndexMap[busRow.entity_id] = index
            }
        }
        val stopInArr = mutableListOf<String>()
        entities.forEach { entity ->

            if (jsonStops?.has(entity.vehicle.trip.routeId.toString()) == true) {
                val lat1 = entity.vehicle.position.latitude.toDouble()
                val lon1 = entity.vehicle.position.longitude.toDouble()
                val bus_distance = calculateDistance(lat1,lon1,userLat,userLon)
                if(bus_distance <= 5){

                    var isCompleted = false
                    val stopRoutes = jsonStops?.get(entity.vehicle.trip.routeId.toString())
                    val stopArr = JSONArray(stopRoutes.toString())
                    var next_km = 0.0
                    var nextStop = ""
                    var source = ""
                    var destination = ""
                    var visitedCount = 0

                    var stop_distance=0.0
                    var prev_lat = 0.0
                    var prev_lon = 0.0
                    var prev_stop_distance = 0.0
                    var bus_stop_distance = 0.0
                    var isBusFound = false
                    var prev_location = ""
                    val stop_list_detail = emptyList<Stops>().toMutableList()
                    var isNextStopCheck=false
                    for (i in 0 until stopArr.length()) {
                        val element = stopArr[i]
                        val stopDetail = stopDetails?.get(element.toString())
                        val stopDetailArr = JSONObject(stopDetail.toString())
                        val lat2: Double? = stopDetailArr["stop_lat"] as? Double
                        val lon2: Double? = stopDetailArr["stop_lon"] as? Double
                        if (lat2 != null && lon2 != null ) {
                            var isNextStop=false

                            var isVisited = false
                            if(i==0){
//                                if (calculateDistance(lat1,lon1,lat2,lon2) > 0.10){
                                    isVisited = true
                                visitedCount++
//                                }
                                source = stopDetailArr["stop_name"].toString()
                            }else{
                                prev_stop_distance = calculateDistance(prev_lat,prev_lon,lat2,lon2)
                                stop_distance += prev_stop_distance
                                if(!isBusFound){
                                    bus_stop_distance = calculateDistance(prev_lat,prev_lon,lat1,lon1)
                                    if (bus_stop_distance > prev_stop_distance){
                                        isVisited = true
                                        visitedCount++
                                    }else{
                                        if(!isNextStopCheck){
                                            isNextStopCheck=true
                                            isNextStop=true
                                        }
                                        next_km = prev_stop_distance - bus_stop_distance
                                        isBusFound=true
                                        nextStop = stopDetailArr["stop_name"].toString()
                                    }
                                }
                            }
                            stop_list_detail += Stops(
                                stopDetailArr["stop_id"].toString(),
                                stopDetailArr["stop_name"].toString(),
                                lat2,
                                lon2,
                                stopDetailArr["stop_code"].toString(),
                                isVisited,
                                stop_distance,
                                isNextStop
                            )
                            prev_lat = lat2
                            prev_lon = lon2
                            prev_location = stopDetailArr["stop_name"].toString()
                            if(i+1==stopArr.length()){
                                destination = stopDetailArr["stop_name"].toString()
                            }
                            if((stopList.size<=0) && !(stopDetailArr["stop_id"].toString() in stopInArr)){
                                stopInArr.add(stopDetailArr["stop_id"].toString())
                                val stopsMeta = StopsMeta(stopDetailArr["stop_id"].toString(), stopDetailArr["stop_name"].toString(), LatLng(lat2,lon2), stopDetailArr["stop_code"].toString())
                                newStopList.add(stopsMeta)
                            }
                        }
                    }


                    var stops_remaning = stopArr.length() - visitedCount
                    if(visitedCount==stopArr.length()){
                        isCompleted = true
                        stops_remaning = 0
                        nextStop = prev_location
                    }
                    if ((liveBusData.size>0) && tripIndexMap.containsKey(entity.id)){

                        if(entity.id=="DL1PC7344"){
                            println("entity insert--------------------")
                            println(entity)
                        }
                        val busIndex = tripIndexMap[entity.id]
                        busList[busIndex!!].bus_distance = bus_distance
                        busList[busIndex].bus_stop_distance = bus_stop_distance
                        busList[busIndex].next_km = next_km
                        busList[busIndex].nextStop = nextStop
                        busList[busIndex].source = source
                        busList[busIndex].destination = destination
                        busList[busIndex].isCompleted = isCompleted
                        busList[busIndex].stops_remaning = stops_remaning
                        busList[busIndex].stop_list = stop_list_detail
                        busList[busIndex].latitude = entity.vehicle.position.latitude.toDouble()
                        busList[busIndex].longitude = entity.vehicle.position.longitude.toDouble()
                    }else{
                        if(entity.id=="DL1PC7344"){
                            println("entity insert--------------------")
                            println(entity)
                        }
                        busList += Bus(
                            entity.id,
                            entity.vehicle.trip.tripId,
                            entity.vehicle.trip.startTime,
                            entity.vehicle.trip.startDate,
                            entity.vehicle.trip.routeId,
                            entity.vehicle.position.latitude.toDouble(),
                            entity.vehicle.position.longitude.toDouble(),
                            entity.vehicle.vehicle.id,
                            entity.vehicle.vehicle.label,
                            bus_distance,
                            bus_stop_distance,
                            next_km,
                            nextStop,
                            source,
                            destination,
                            isCompleted,
                            stops_remaning,
                            stop_list_detail
                        )
                    }

//                            put("stop_list", JsonArray(emptyList()))

                }

            }
        }

        println("---------------finalData")
        println(busList.size)
        if (liveBusData.size>0) {
            return busList to newStopList
        }
        val sortedBusList = busList.sortedBy { it.trip_id }
        return sortedBusList to newStopList
    }

    return liveBusData to newStopList
}
fun closestBusIndex(liveBusData: List<Bus>): Int{
    var minIndex = 0
    var minDistance = liveBusData[minIndex].bus_distance
    liveBusData.forEachIndexed { index, busRow ->
        if(busRow.bus_distance<minDistance){
            minDistance = busRow.bus_distance
            minIndex = index
        }
    }
    println("Closest----------------------")
    println(minDistance)
    println(minIndex)
    println(liveBusData[minIndex])
    return minIndex
}
fun loadStopsMeta(stopDetails: List<StopsMeta>,lat: Double, lon: Double): List<StopsMeta> {
    print("---------------------------------------Stops")
    print(stopDetails)
    val stopsMetaList = mutableListOf<StopsMeta>()
    println(stopDetails.size)
    stopDetails.forEach { stop ->
        if(calculateDistance(stop.location.latitude,stop.location.longitude,lat,lon) <= 2){
            stopsMetaList.add(stop)
        }
    }
    println("Size-----------------------")
    println(stopsMetaList.size)
    return stopsMetaList
}