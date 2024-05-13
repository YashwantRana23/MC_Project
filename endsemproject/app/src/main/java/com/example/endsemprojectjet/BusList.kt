package com.example.endsemprojectjet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun BusList(liveBusData: List<Bus>, busAutoSelected: Int,
            onBusSelect: (Int) -> Unit){
    Row(modifier = Modifier.fillMaxSize()){
        Column (modifier = Modifier
            .fillMaxWidth()){
            if(busAutoSelected>0){
                busRow(liveBusData[busAutoSelected],busAutoSelected,busAutoSelected,onBusSelect)
            }
            LazyColumn {
                itemsIndexed(items = liveBusData) { index, item ->
                    if(!item.isCompleted && String.format("%.1f", item.next_km).toFloat()>0 && item.nextStop!=""){
                        if(index!=busAutoSelected){
                            busRow(item,index,busAutoSelected,onBusSelect)
                        }
                    }

                }
            }

        }
    }
}
@Composable
fun busRow(item: Bus, index: Int, busAutoSelected: Int,
           onBusSelect: (Int) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(if (busAutoSelected==index) Color.Gray else Color.Transparent)
            .drawBehind {
                val strokeWidth = 1 * density
                val y = size.height - strokeWidth / 2

                drawLine(
                    Color(0xAADDDDDD),
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
            }.clickable {
                println("Clicked")
                println(index)
                println(item)
                onBusSelect(index)
            }
        ,
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth(0.25f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                text = String.format("%.1f", item.next_km)+" Km")
        }


        // Middle column for Next Station and Bus Number
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ){
            //    Text(text = time)
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.Bottom
            ){
                Text(
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    text = item.nextStop)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp).padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    style = TextStyle(fontSize = 12.sp),
                    text = item.source+" - "+item.destination)
            }
        }


        // Right column for Distance
        Column (
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                text = item.stops_remaning.toString())
        }
    }
}
@Composable
fun StopList(bus: Bus) {
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn {
            itemsIndexed(bus.stop_list) { index , stop ->
                showStopRow(stop = stop, stopSize = bus.stop_list.size, index = index)
            }
        }
    }
}
@Composable
fun showStopRow(stop: Stops, stopSize: Int, index: Int){
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier.fillMaxWidth()
                .background(if (stop.isNextStop) Color.Gray else Color.Transparent)
            ,

        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .padding(horizontal = 12.dp),
            ){
                Text(
                    text = "${(ceil(stop.stop_distance * 100) / 100)} km",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    textAlign = TextAlign.End,
                    style = TextStyle(fontSize = 16.sp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .drawWithContent {
                        drawContent()
                        if (index + 1 < stopSize) {
                            drawRect(
                                color = if (stop.isVisited == true) Color.Green else Color.White,
                                size = Size(3.dp.toPx(), size.height),
                                topLeft = Offset(0f, 50f)
                            )
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${stop.stop_name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                    ,
                    color = Color.White,
                    style = TextStyle(fontSize = 18.sp)

                )
            }
        }
    }

}
