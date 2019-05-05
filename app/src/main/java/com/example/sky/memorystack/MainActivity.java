package com.example.sky.memorystack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static ArrayList<String> places=new ArrayList<String>();
    static ArrayList<LatLng> locations=new ArrayList<LatLng>();
    static ArrayAdapter<String> placeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.sky.memorystack", Context.MODE_PRIVATE);
        ArrayList<String> latitude= new ArrayList<>();
        ArrayList<String> longitude= new ArrayList<>();

        places.clear();
        latitude.clear();
        longitude.clear();
        locations.clear();

        try{
            places= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitude= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitude",ObjectSerializer.serialize(new ArrayList<String>())));
            longitude= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitude",ObjectSerializer.serialize(new ArrayList<String>())));

        }catch (Exception e){
            e.printStackTrace();
        }

        if(places.size()>0 && latitude.size()>0 && longitude.size()>0){
            if (places.size() == latitude.size()&& places.size() == longitude.size()){
               for(int i=0;i<latitude.size();i++){
                   locations.add(new LatLng(Double.parseDouble(latitude.get(i)),Double.parseDouble(longitude.get(i))));
               }
            }


        }
        else{
            places.add("Add a new Memory place");
            locations.add(new LatLng(0,0));
        }

        ListView listView=findViewById(R.id.List);
        placeList=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,places);
        listView.setAdapter(placeList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Intent toMap=new Intent(getApplicationContext(),MapsActivity.class);
                    toMap.putExtra("PlaceNum",i);
                    startActivity(toMap);

            }
        });
    }
}
