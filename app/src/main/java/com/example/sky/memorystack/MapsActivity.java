package com.example.sky.memorystack;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
LocationManager locationManager;
LocationListener locationListener;

    private GoogleMap mMap;

    public void centerMapOnLoc(Location location,String title){
        LatLng userLoc=new LatLng(location.getLatitude(),location.getLongitude());
        //mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLoc).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,10));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLoc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLoc(lastKnownLoc,"Current Location");
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent=getIntent();
        int posNum=intent.getIntExtra("PlaceNum",0);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addressList;

                String address="";
                try{
                    addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(addressList!=null && addressList.size()>0){
                        if(addressList.get(0).getThoroughfare()!=null){
                            if(addressList.get(0).getSubThoroughfare()!=null){
                                address+=addressList.get(0).getSubThoroughfare()+" ";
                            }
                            address+=addressList.get(0).getThoroughfare();
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(address.equals("")){
                    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm dd-MM-yyyy");
                    address+=sdf.format(new Date());
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                MainActivity.places.add(address);
                MainActivity.locations.add(latLng);
                MainActivity.placeList.notifyDataSetChanged();
                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.example.sky.memorystack",Context.MODE_PRIVATE);

                try {
                    ArrayList<String> latitude= new ArrayList<>();
                    ArrayList<String> longitude=new ArrayList<>();
                    for(LatLng cords:MainActivity.locations){
                        latitude.add(Double.toString(cords.latitude));
                        longitude.add(Double.toString(cords.longitude));
                    }
                    sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
                    sharedPreferences.edit().putString("latitude",ObjectSerializer.serialize(latitude)).apply();
                    sharedPreferences.edit().putString("longitude",ObjectSerializer.serialize(longitude)).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                Toast.makeText(getApplicationContext(),"Memory Saved!",Toast.LENGTH_SHORT).show();

            }
        });
        if(posNum==0){
            //Zoom on user curr location
            locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLoc(location,"Current Location");
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

            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLoc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLoc(lastKnownLoc,"Current Location");
            }
            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
        else{
            Location placeLoc=new Location(LocationManager.GPS_PROVIDER);
            placeLoc.setLatitude(MainActivity.locations.get(intent.getIntExtra("PlaceNum",0)).latitude);
            placeLoc.setLongitude(MainActivity.locations.get(intent.getIntExtra("PlaceNum",0)).longitude);
            centerMapOnLoc(placeLoc,MainActivity.places.get(intent.getIntExtra("PlaceNum",0)));
        }
    }
}
