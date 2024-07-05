package com.example.fahrenheit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import java.util.Locale;
import android.os.Build;
import android.view.ViewGroup;
import android.view.View;
import android.graphics.Color;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    TextView tempID;
    TextView dateID;
    TextView cityID;
    TextView weatherDescID;
    ImageView weatherImageView;
    SwitchCompat simpleSwitch;
    String temperatureUnit;
    TextView ForC;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            getLocation();
        } else {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        tempID = findViewById(R.id.tempID);
        weatherDescID = findViewById(R.id.WeatherDescID);
        dateID = findViewById(R.id.DateID);
        cityID = findViewById(R.id.CityID);
        weatherImageView=(ImageView)findViewById(R.id.WeatherPic);
        dateID.setText(getCurrentDate());
        simpleSwitch=(SwitchCompat)findViewById(R.id.switch1);
        ForC=(TextView)findViewById(R.id.textView2);
        updateTemperatureUnits();
        updateBackground();

        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTemperatureUnits();
                getLocation();
            }
        });

    }
    private void updateTemperatureUnits(){
        if(simpleSwitch.isChecked()){
            temperatureUnit="Imperial";
            ForC.setText("F");

        }
        else{
            temperatureUnit="metric";
            ForC.setText("C");
        }
    }


    private void getLocation() {

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);


        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude= location.getLongitude();
                    String url="https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=9051a86011f59512d361347a1ed4da80";
                    fetchWeatherData(url);
                }
            }
        }
    };



    private void fetchWeatherData(String url) {
        url+="&units="+temperatureUnit;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseObject) {
                        try {
                            JSONObject mainJSONObject = responseObject.getJSONObject("main");
                            JSONArray weatherArray = responseObject.getJSONArray("weather");
                            JSONObject firstWeatherObject = weatherArray.getJSONObject(0);
                            String temp = Integer.toString((int) Math.round(mainJSONObject.getDouble("temp")));
                            String weatherDescription = firstWeatherObject.getString("description");
                            String city = responseObject.getString("name");

                            runOnUiThread(new Runnable(){
                                    @Override
                                    public void run(){
                                        tempID.setText(temp);
                                        weatherDescID.setText(weatherDescription);
                                        cityID.setText(city);
                                        int iconResourceId = getResources().getIdentifier("icon_" + weatherDescription.replace(" ", ""), "drawable", getPackageName());
                                        weatherImageView.setImageResource(iconResourceId);
                            }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WeatherApp", "Error fetching weather data: " + error.toString());
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsObjRequest);
    }


    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d");
        String formattedDate = dateFormat.format(calendar.getTime());

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String suffix;
        if (dayOfMonth >= 11 && dayOfMonth <= 13) {
            suffix = "th";
        } else {
            switch (dayOfMonth % 10) {
                case 1:
                    suffix = "st";
                    break;
                case 2:
                    suffix = "nd";
                    break;
                case 3:
                    suffix = "rd";
                    break;
                default:
                    suffix = "th";
            }
        }

        formattedDate += suffix;

        SimpleDateFormat monthFormat = new SimpleDateFormat(" MMMM");
        formattedDate += monthFormat.format(calendar.getTime());

        return formattedDate;
    }

    private void updateBackground() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);


        int backgroundResource = hour >= 18 || hour < 6 ? R.drawable.background3 : R.drawable.background2;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View contentView = findViewById(android.R.id.content);
            contentView.setBackground(ContextCompat.getDrawable(this, backgroundResource));

            if (hour >= 18 || hour < 6) {
                changeTextColor(Color.WHITE);
            } else {
                changeTextColor(Color.BLACK);
            }
        } else {
            findViewById(android.R.id.content).setBackgroundDrawable(ContextCompat.getDrawable(this, backgroundResource));
        }
    }

    private void changeTextColor(int color) {
        // Find and change text color of all TextViews in the layout
        ViewGroup viewGroup = findViewById(android.R.id.content);
        changeTextColorInViewGroup(viewGroup, color);
    }

    private void changeTextColorInViewGroup(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                changeTextColorInViewGroup((ViewGroup) child, color);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation();
            } else {

                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                findViewById(R.id.locationPermissionMessage).setVisibility(View.VISIBLE);
            }
        }
    }
}
