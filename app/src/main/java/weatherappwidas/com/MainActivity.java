package weatherappwidas.com;

import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import weatherapp.com.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Handler handler;

    String city;

    EditText edtLocation;

    TextView current_temperature_field;

    private SupportMapFragment mapFragment;

    static GoogleMap googleMap;

    private static final int REQUEST_CODE_PERMISSION = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Marshmallow+
            ActivityCompat.requestPermissions(this, new String[]{

                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,

            }, REQUEST_CODE_PERMISSION);

        } else {
            // Pre-Marshmallow
        }


        handler = new Handler();

        edtLocation = (EditText) findViewById(R.id.edt_location);

        current_temperature_field = (TextView) findViewById(R.id.current_temperature_field);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap));

        mapFragment.getMapAsync(this);

        googleMap = mapFragment.getMap();


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                Toast.makeText(getApplicationContext(), point.toString(), Toast.LENGTH_SHORT).show();

                double lat = point.latitude;
                double lng = point.longitude;

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cityName = addresses.get(0).getAddressLine(0);
                Log.d("mmm","Display city name"+cityName);


                updateWeatherData(cityName);


            }
        });




        edtLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                updateWeatherData(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(getApplicationContext(), city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {

                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }


    private void renderWeather(JSONObject json) {
        try {

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            current_temperature_field.setText(

                    json.getString("name").toUpperCase(Locale.US) +
                            ", " +
                            json.getJSONObject("sys").getString("country") + "\n"+
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa" +

                            "\n" + String.format("%.2f", main.getDouble("temp"))+ " ℃"+

                    "\n" +"Last update: " + updatedOn);




        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap1) {
        googleMap = googleMap1;
    }
}
