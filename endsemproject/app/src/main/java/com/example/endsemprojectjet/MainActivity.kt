package com.example.endsemprojectjet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.endsemprojectjet.ui.MapScreen
import com.example.endsemprojectjet.ui.theme.EndsemprojectjetTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.protobuf.InvalidProtocolBufferException
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    private val coroutineSupervisor = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val API_URL = "https://otd.delhi.gov.in/api/realtime/VehiclePositions.pb?key=tvKJQxkw13wVPHGWNwgK0L75xQxsec01"

    var liveBusData by mutableStateOf<List<Bus>>(emptyList())
    var jsonStops by mutableStateOf<JSONObject?>(null)
    var stopDetails by mutableStateOf<JSONObject?>(null)
    var StopsMeta by mutableStateOf<List<StopsMeta>>(emptyList())
    var userLat by mutableStateOf(0.0)
    var userLon by mutableStateOf(0.0)
    var selectedTab by mutableStateOf("buslist")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var isInBus by mutableStateOf(false)

    var busSelected by mutableStateOf(-1)
    var busAutoSelected by mutableStateOf(-1)

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myJsonLoader = MyJsonLoader(this) // Assuming 'this' is a valid context
        jsonStops =  myJsonLoader.loadJsonFromFile("stops.json")
        stopDetails =  myJsonLoader.loadJsonFromFile("stop_details.json")
       // StopsMeta = stopDetails?.let { loadStopsMeta(it) }!!

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            getLastLocation()
        }
        coroutineSupervisor.launch {
            while (true) {
                if(!isInBus){
                    getLastLocation()
                }
                delay(5000)
            }
        }
        setContent {
            EndsemprojectjetTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    loadData()
                    LandingScreen(modifier = Modifier.fillMaxSize(),this)
                }
            }
        }
    }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun loadData() {
        coroutineSupervisor.launch {
            while(true){
                val startTime = System.currentTimeMillis()
                val liveData = fetchData()
                if (liveData!=null){
                    val (updatedLiveBusData, updatedTempStopsData) = processData(liveData, stopDetails, jsonStops, userLat, userLon, liveBusData, StopsMeta)
                    liveBusData = updatedLiveBusData
                    if(isInBus && busAutoSelected > 0){
                        userLat = updatedLiveBusData[busAutoSelected].latitude
                        userLon = updatedLiveBusData[busAutoSelected].longitude
                    }
                    println(updatedTempStopsData.size)
                    if(updatedTempStopsData.size>0){
                        StopsMeta = loadStopsMeta(updatedTempStopsData,userLat,userLon)
                    }

                }
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime

            println("Execution time: $executionTime ms")
                if(liveData===null){
                    delay(1000)
                }else{
                    delay(10000)
                }
            }
        }
    }
    private fun fetchData(): FeedMessage? {
        return try {
            val url = URL(API_URL)
            FeedMessage.parseFrom(url.openStream())
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
            null
        }
    }
    private fun getLastLocation() {
        println("getLastLocation---------------------")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                println("inFun---------------------")
                if (location != null) {
                    userLat = location.latitude
                    userLon = location.longitude
                } else {
                    println("Location is null")
                }
            }
            .addOnFailureListener { e ->
                println("Error getting location: ${e.message}")
            }
    }

    @Composable
    fun LandingScreen(modifier: Modifier, context: Context){
        Column(modifier = Modifier
            .fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xAA981C1E)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Column(modifier = Modifier) {
                    Text(text = "Are You in a bus ", style = TextStyle(fontSize = 16.sp))
                }
                Column(modifier = Modifier) {
                    ToggleButton(
                        isChecked = isInBus,
                        onCheckedChange = { isChecked ->
                            isInBus = isChecked
                            if(isChecked){
                                busSelected = closestBusIndex(liveBusData)
                                println("userbusdata----------------------------------")
                                println(userLat)
                                println(userLon)
                                println(busSelected)
                                userLat = liveBusData[busSelected].latitude
                                userLon = liveBusData[busSelected].longitude
                                println(liveBusData[busSelected])
                            }else{
                                busSelected = -1
                                getLastLocation()
                            }
                            busAutoSelected = busSelected
                        }
                    )
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.075f)) {
                Column(modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { selectedTab = "buslist"
                            busSelected = -1
                                  },
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                if (selectedTab == "buslist") {
                                    val strokeWidth = 2 * density
                                    val y = size.height - strokeWidth / 2

                                    drawLine(
                                        Color.White,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        strokeWidth
                                    )
                                }
                            },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xAAC01921),
                            contentColor = Color(0xAAFFFFFF),
                        )
                    )
                    {
                        Text(text = "Available bus", style = TextStyle(fontSize = 16.sp))
                    }
                }

                Column(modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { selectedTab = "busmap" },
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                if (selectedTab == "busmap") {
                                    val strokeWidth = 2 * density
                                    val y = size.height - strokeWidth / 2

                                    drawLine(
                                        Color.White,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        strokeWidth
                                    )
                                }
                            },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xAAC01921),
                            contentColor = Color(0xAAFFFFFF),
                        ),
                    ) {
                        Text(text = "Show Map", style = TextStyle(fontSize = 16.sp))
                    }
                }
            }
            if (selectedTab == "buslist") {
                if(busSelected<0){
                    if(liveBusData.size==0){
                        Column (
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            CircularProgressIndicator()
                        }
                    }else{
                        BusList(liveBusData,
                        busAutoSelected,
                        onBusSelect = { newBusSelect ->
                            busSelected = newBusSelect
                        })
                    }
                }else{
                    StopList(liveBusData[busSelected])
                }
            }else{
                MapScreen(context,StopsMeta, LatLng(userLat,userLon),liveBusData)
            }
        }
    }
}


