package com.example.localisation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;


public class MainActivity extends AppCompatActivity {

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;

    private TelephonyManager tm;
    RequestQueue requestQueue;
    String insertUrl = "http://192.168.0.114/localisation/ws/createPosition.php";

    // Define a constant for the permission request code
    private static final int REQUEST_PHONE_STATE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Button button = findViewById(R.id.button);

        // Set a click listener for the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the MapsActivity
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 150, new
                LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        accuracy = location.getAccuracy();
                        @SuppressLint("StringFormatMatches") String msg = String.format(
                                getResources().getString(R.string.new_location), latitude,
                                longitude, altitude, accuracy);
                        addPosition(latitude, longitude);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        String newStatus = "";
                        switch (status) {
                            case LocationProvider.OUT_OF_SERVICE:
                                newStatus = "OUT_OF_SERVICE";
                                break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                newStatus = "TEMPORARILY_UNAVAILABLE";
                                break;
                            case LocationProvider.AVAILABLE:
                                newStatus = "AVAILABLE";
                                break;
                        }
                        String msg = String.format(getResources().getString(R.string.provider_new_status),
                                provider, newStatus);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        String msg = String.format(getResources().getString(R.string.provider_enabled),
                                provider);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        String msg = String.format(getResources().getString(R.string.provider_disabled),
                                provider);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Gérer la réponse du serveur ici, si nécessaire
                        // Par exemple, afficher un toast en cas de succès
                        Toast.makeText(getApplicationContext(), "Envoi réussi", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Gérer les erreurs ici
                        if (error instanceof TimeoutError) {
                            Toast.makeText(getApplicationContext(), "Erreur de délai d'attente", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(), "Pas de connexion réseau", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(), "Erreur d'analyse", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erreur inconnue", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                int permission=ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE);
//                if(permission==PackageManager.PERMISSION_GRANTED){
//                    tm=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                    imei=tm.getDeviceId().toString();
//                }
//                else{
//                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_PHONE_STATE},123);
//                }


                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", "ime");

                return params;
            }
        };

        requestQueue.add(request);
    }
}

