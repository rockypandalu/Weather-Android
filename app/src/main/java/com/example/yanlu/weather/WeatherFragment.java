package com.example.yanlu.weather;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import org.json.JSONObject;

/**
 * Created by yanlu on 16/3/19.
 * The WeatherFragment is a Fragment built on MainActivity
 * It calls getJSON method in GetWeather Class to get current weather and weather in the next 10 days
 * It sets onClickListener on imageButton (shows current weather), when click the app update current weather
 * It sets onItemClickListener on ListView, when click a cell, the app start a new activity with animation to show detailed weather
 * It heritages SQLite class and insert future weather data to SQLite
 * The transition animation is customized when transfer from WeatherFragment to DetailActivity
 * If using real phone, the app can use the phone location to report weather, if using emulator the app use New York Location
 * because the Emulator does not support location information
 */
public class WeatherFragment extends Fragment implements LocationListener {
    ImageButton curImgBtn;
    TextView weatherField;
    TextView dateField;
    TextView tempField;
    ListView weatherListView;
    private Handler handler1;
    private Handler handler2;
    private String LatLong;
    private SQLite db;
    private Location location;
    private LocationManager locationManager;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();

    public WeatherFragment(){
        handler1 = new Handler();
        handler2 = new Handler();

    }

    /**
     * Initial SQLite database in onCreate Stage
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLite(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current, container, false);
        weatherField = (TextView)rootView.findViewById(R.id.textViewWeather);
        dateField = (TextView)rootView.findViewById(R.id.textViewDate);
        tempField = (TextView) rootView.findViewById(R.id.textViewTemp);
        weatherListView = (ListView)rootView.findViewById(R.id.listViewWeather);
        curImgBtn = (ImageButton)rootView.findViewById(R.id.imageButtonWeather);

        /**
         * The following code is used to get current location
         * The get location function might not suitable for Emulator
         * Thus I also provide default location for Emulator
         */
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        if (location != null){
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();
            LatLong = "lat="+lat+"&lon="+lon+"";
            Log.d("location",LatLong);
        }
        else{
            LatLong = "lat=40.8&lon=-73.95";
            Log.d("location","default");
        }

        /**
         * Sets onClickListener on imageButton (shows current weather), when click the app update current weather
         * The weather information about the next 10 days will not be updated, and kept in SQLite
         */
        curImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurWeather(LatLong);
                Toast.makeText(getActivity(), "Current Weather Updated", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Sets onItemClickListener on ListView, when click a cell, the app start a new activity with customized animation to show detailed weather
         */
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub

                int pos = position + 1;
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", pos);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                /**
                 * Customize animation for transition
                 */
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);

            }
        });
        /**
         * Update weather information (current & next 10 days), store data and show data
         */
        updateWeather(LatLong);
        updateCurWeather(LatLong);
        return rootView;
    }


    /**
     * Update current weather data using API
     * @param city
     */
    private void updateCurWeather(final String city){
        new Thread(){
            public void run(){
                final JSONObject cur_json = GetWeather.getJSON(getActivity(), city, "http://api.openweathermap.org/data/2.5/weather?%s");

                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        showUpdate(cur_json);

                    }
                });
            }

//            }
        }.start();
    }

    /**
     * Update daily weather in the next 10 days using API
     * @param city
     */
    private void updateWeather(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = GetWeather.getJSON(getActivity(), city, "http://api.openweathermap.org/data/2.5/forecast/daily?%s&cnt=10&mode=json");

                handler1.post(new Runnable() {
                    @Override
                    public void run() {
                        showFutureUpdate(json);
                        db.deleteAll();
                        updateSQL(json);
                    }
                });
                }

//            }
        }.start();
    }

    /**
     * Extract information in jSONObject, and show current weather on screen
     * @param json
     */
    private void showUpdate(JSONObject json){
        try{
//            Toast.makeText(getActivity(), json.toString(), Toast.LENGTH_LONG).show();
            float cur_temp = Float.parseFloat(json.getJSONObject("main").getString("temp"));
            int temp_int = (int)((cur_temp - 273.15)*1.8+32);
            JSONObject weather_info = json.getJSONArray("weather").getJSONObject(0);
            curImgBtn.setImageResource((Integer) ImageSelect.weatherMainIcon(weather_info.getString("icon").toString()));
            weatherField.setText(weather_info.getString("main"));
            dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            tempField.setText(Integer.toString(temp_int) + "℉");
        }catch(Exception e){
            updateCurWeather(LatLong);
            Log.d("CurrentWeather", "One or more fields not found in the JSON data");
        }
    }

    /**
     * Extract information in jSONObject, show future weather in ListView on screen
     * @param json
     */
    private void showFutureUpdate(JSONObject json){
        try{
//            Toast.makeText(getActivity(), json.toString(), Toast.LENGTH_LONG).show();
            listItem = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i=0;i<10;i++){
                HashMap<String, Object> map = new HashMap<>();
                Date rawDay = calendar.getTime();
                JSONObject thisDay = json.getJSONArray("list").getJSONObject(i);
                String High = kToC(thisDay.getJSONObject("temp").getString("max"));
                String Low = kToC(thisDay.getJSONObject("temp").getString("min"));
                map.put("ItemImg", ImageSelect.weatherIcon(thisDay.getJSONArray("weather").getJSONObject(0).getString("icon")));
                map.put("ItemDate", dayFormat.format(rawDay));
                map.put("ItemTemp", Low+"℉"+"-"+High+"℉");
                map.put("ItemWeather",thisDay.getJSONArray("weather").getJSONObject(0).getString("main"));
                listItem.add(map);
                calendar.add(Calendar.DAY_OF_YEAR,1);
            }

            SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(),listItem,R.layout.list_item, new String[]{"ItemImg","ItemDate","ItemTemp","ItemWeather"},new int[]{R.id.img,R.id.listDate,R.id.listTemp,R.id.listWeather});
            weatherListView.setAdapter(listItemAdapter);
        }catch(Exception e){
            updateWeather(LatLong);
            Log.d("FutureWeather", "One or more fields not found in the JSON data");

        }
    }

    /**
     * Extract information about future daily weather from JSONObject, load them into SQLite database
     * @param json
     */
    public void updateSQL(JSONObject json){
        try {
            for (int i = 0; i < 10; i++) {
                JSONObject thisDay = json.getJSONArray("list").getJSONObject(i);
                JSONObject temp = thisDay.getJSONObject("temp");
                String icon = thisDay.getJSONArray("weather").getJSONObject(0).getString("icon");
                String max = kToC(temp.getString("max"));
                String min = kToC(temp.getString("min"));
                String pressure = thisDay.getString("pressure");
                String speed = thisDay.getString("speed");
                String humidity = thisDay.getString("humidity");
                String weather = thisDay.getJSONArray("weather").getJSONObject(0).getString("main");
                String description = thisDay.getJSONArray("weather").getJSONObject(0).getString("description");
                db.insert(weather, description, max, min, icon, pressure, speed, humidity);
            }
            Toast.makeText(getActivity(), "Weather Update Succeed", Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Log.e("DB_Failure", "fail to update DB");

        }

    }

    /**
     * Change temperature unit from Kelvin to Fahrenheit
     * @param k
     * @return
     */
    private String kToC(String k){
        float cur_temp = Float.parseFloat(k);
        int temp_int = (int)((cur_temp - 273.15)*1.8+32);
        return Integer.toString(temp_int);
    }

    @Override
    public void onLocationChanged(Location location) {
//        txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:"
//                + location.getLongitude());
        Toast.makeText(getActivity(), Double.toString(location.getLatitude()), Toast.LENGTH_LONG).show();
        Log.d("haha",Double.toString(location.getLatitude()));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }
}
