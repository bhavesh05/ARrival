package com.p532.arbusschedules;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Created by vidyachitta on 12/3/17.
 */

public class BusDataRetrieval {

    Context actContext;
    LocationManager locationManager;
    private boolean stopFound ;
    private String stopID;
    private List<String> busList;
    private  Map<String,String> timeMap;
    private List<BusDetail> busDetailList;
    private BusDetailAdapter busDetailAdapter;

    public BusDataRetrieval(Context actContext,LocationManager locationManager,  Map<String,String> timeMap, List<BusDetail> busDetailList, BusDetailAdapter busDetailAdapter){
        this.locationManager=locationManager;
        this.actContext=actContext;
        this.timeMap = timeMap;
        this.busDetailList = busDetailList;
        this.busDetailAdapter = busDetailAdapter;

    }
    void getBusData(){


//      storeStop();
//      storeBus();
//      storeBusStops();
//      storeBusTimings();






        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(actContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(actContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    //get the latitude and longitude from the current location
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.d("MainActivity", "onLocationChanged: latitude: " + latitude+"longitude: "+longitude+"========>");

                    Log.d("MainActivity", "onLocationChanged: "+"Calling getStops"+"========>");
                    getStops(new GeoLocation(latitude,longitude));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }else

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.d("MainActivity", "onLocationChangedGPS: latitude: " + latitude+"longitude: "+longitude+"========>");

                    Log.d("MainActivity", "onLocationChangedGPS: "+"Calling getStops"+"========>");
                    getStops(new GeoLocation(latitude,longitude));

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }


    }
    private void getStops(GeoLocation geoLocation) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Stops");
        GeoFire geoFire = new GeoFire(ref);
        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation,1000);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!stopFound){
                    stopFound = true;
                    stopID = key;
                    //stopNameText.append("You are at the stop: "+key.toUpperCase()+"\n");
                    Log.d("getStops", "onKeyEntered: "+"StopID is set to: "+ stopID.toUpperCase()+"========>");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!stopFound){
                    Log.d("getStops", "onGeoQueryReady: "+"No Stops Nearby"+"========>");
                    //stopNameText.append("No Stops Nearby");
                }else {
                    Log.d("getStops", "onGeoQueryReady: "+"Calling getBus"+"========>");
                    getBus(stopID);


                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


    private void getBus(final String stopID) {

        Log.d("getBus", "StopID received: "+stopID+"========>");
        DatabaseReference busStopref = FirebaseDatabase.getInstance().getReference().child("BusStops");
        Query busIDQuery = busStopref.orderByChild("stop_id").equalTo(stopID);

        busList = new ArrayList<>();
        busIDQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot record :dataSnapshot.getChildren()){
                    Log.d("getBus", "onDataChange: "+record.getValue()+"========>");
                    for (DataSnapshot attribute :record.getChildren()) {
                        if (attribute.getKey().toString().equals("bus_id")){
                            busList.add(attribute.getValue().toString());
                        }
                    }
                }
               // stopNameText.append("The buses at this stop are: "+busList.toString()+"\n");
                Log.d("getBus", "onDataChange: "+busList.toString()+"========>");
                Log.d("getBus", "onDataChange: "+"Calling getBusTimings"+"========>");

                getBusTimings(busList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("getBus", "onCancelled: databaseError " );
            }
        });
    }

    private void getBusTimings(final List<String> busList) {

        busDetailList.clear();

        Log.d("getBusTimings", "The bus list received: "+busList.toString()+"========>");
        DatabaseReference busTimingRef = FirebaseDatabase.getInstance().getReference().child("BusTimings");
        // final String bid = busList.get(0);
        //  Log.d("getBusTimings", "The bus  bid: "+bid+"========>");
//        stopNameText.append("The buses in next 15 minutes are:\n");
        for (final String bid:busList){
            Query busTimeQuery =  busTimingRef.orderByChild("busnumber").equalTo(bid.trim());//.orderByChild("bustiming").startAt("10:00").endAt("10:15");
           // Query busTimeQuery2= busTimeQuery.orderByChild("bustiming");
            busTimeQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot record:dataSnapshot.getChildren()){

                        for (DataSnapshot attribute:record.getChildren()){
                            Log.d("getBusTimings2", "onDataChange: "+attribute.getKey()+"========>");
                            Log.d("getBusTimings2", "onDataChange: "+attribute.getValue()+"========>");
                            if (attribute.getKey().equals("bustiming" )){
                                if ( getDiff(  attribute.getValue().toString())){
                                   // stopNameText.append("1.  "+record.child("busnumber").getValue().toString()+"   "+attribute.getValue().toString()+"  ");
                                    BusDetail busDetail = new BusDetail(bid, attribute.getValue().toString());
                                    busDetailList.add(busDetail);
                                    busDetailAdapter.notifyDataSetChanged();
                                    timeMap.put(bid,attribute.getValue().toString());
                                }
                            }
                        }
                    }
                    Log.d("getBusTimings2", "onDataChange: outside for loop " + timeMap+"========>");
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private boolean getDiff(String key){
        String timeString = key;
        String [] tokens =timeString.split(":");
        int busHour = Integer.parseInt(tokens[0]);
        int busMinute = Integer.parseInt(tokens[1]);
        int busMin = busHour*60+busMinute;

        Date date = new Date();   // given date
        Calendar calendar = Calendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int minute =   calendar.get(Calendar.MINUTE);       // gets month number, NOTE this is zero based!
        int nowMin = hour*60+minute;

        int timeDiff = busMin-nowMin;

        Log.d("getDiff", "current time: "+nowMin);
        Log.d("getDiff", "busmin: "+busMin);
        Log.d("getDiff", "Difference is: "+timeDiff);

        if(timeDiff >=0 && timeDiff<15 )
            return true;
        else{
            busMin += 1440;
            timeDiff = busMin-nowMin;
            if (timeDiff >= 0 && timeDiff < 15)
                return true;
            return false;
        }
    }

}

