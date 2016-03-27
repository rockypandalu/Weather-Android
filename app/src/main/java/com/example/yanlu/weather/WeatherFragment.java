package com.example.yanlu.weather;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
 * When the phone has enabled location, the app will use current lcoation to report weather and renew the data in shared preference
 * If the phone has disabled location, the app use shared preference data
 * If using emulator the app use New York Location (as default value)
 * because the Emulator does not support location information
 */
public class WeatherFragment extends Fragment implements LocationListener {
    ImageButton curImgBtn;
    TextView weatherField;
    TextView dateField;
    TextView tempField;
    ListView weatherListView;
    private Handler handler;
    private String LatLong;
    private SQLite db;
    private Location location;
    private LocationManager locationManager;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();


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
        Log.d("stop","onCreate");
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
            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            if (location == null){
                showLocationAlert();
            }
        }
        if (location != null){
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();
            LatLong = "lat="+lat+"&lon="+lon+"";
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor stored_location = sharedPref.edit();
            stored_location.putString(getString(R.string.location),LatLong);
            stored_location.commit();
            Log.d("location", LatLong);
        }
        else{
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            LatLong = sharedPref.getString(getString(R.string.location),"lat=40.8&lon=-73.95");
            Log.d("get",LatLong);
            Log.d("location","default");
        }

        /**
         * Sets onClickListener on imageButton (shows current weather), when click the app update current weather
         * The weather information about the next 10 days will not be updated, and kept in SQLite
         */
        curImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurWeather cur_weatherTask = new updateCurWeather();
                cur_weatherTask.execute(LatLong);
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
        updateWeather weatherTask = new updateWeather();
        weatherTask.execute(LatLong);

        return rootView;
    }

    /**
     * If the location is disabled, the app show alert to ask whether to enable location and use the last location
     * Or use the old location data catched in the last time when location was enabled.
     * The old location data were stored and updated in Shared Preference
     */
    private void showLocationAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder
                .setMessage("Do you want to renew the location?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                getActivity().startActivity(callGPSSettingIntent);
                                getActivity().finish();
                            }
                        });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(), "Displaying Weather in Old Location", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    /**
     * Update current weather form openWeather API using AsyncTask
     */
    private class updateWeather extends AsyncTask<String, Void, Void> {
        private JSONObject cur_json = null;
        private JSONObject future_json = null;
        @Override
        protected Void doInBackground(String... city){
            while (cur_json == null) {
                cur_json = GetWeather.getJSON(getActivity(), city[0], getActivity().getString(R.string.cur_weather_API));
            }
            while (future_json == null){
                future_json = GetWeather.getJSON(getActivity(), city[0], getActivity().getString(R.string.further_weather_API));
            }
            db.deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            showUpdate(cur_json);
            showFutureUpdate(future_json);
            updateSQL(future_json);
        }
    }

    /**
     * Update future weather form openWeather API using AsyncTask and renew SQLite
     */
    private class updateCurWeather extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... city){
            JSONObject cur_json = null;
            while (cur_json == null){
                cur_json = GetWeather.getJSON(getActivity(), city[0], getActivity().getString(R.string.cur_weather_API));

            }
            return cur_json;
        }

        @Override
        protected void onPostExecute(JSONObject cur_json){
            showUpdate(cur_json);
        }
    }





    /**
     * Extract information in jSONObject, and show current weather on screen
     * @param json
     */
    private void showUpdate(JSONObject json){
        try{
//            Toast.makeText(getActivity(), json.toString(), Toast.LENGTH_LONG).show();
            float cur_temp = Float.parseFloat(json.getJSONObject("main").getString("temp"));
            String temp_int = kToC(Float.toString(cur_temp));
            JSONObject weather_info = json.getJSONArray("weather").getJSONObject(0);
            curImgBtn.setImageResource((Integer) ImageSelect.weatherMainIcon(weather_info.getString("icon").toString()));
            weatherField.setText(weather_info.getString("main"));
            dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            tempField.setText(temp_int + "℉");
        }catch(Exception e){

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
//            updateWeather(LatLong);
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
    @Override
    public void onStop(){
        super.onStop();
        Log.d("stop","Stopped");
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
