package com.example.yanlu.weather;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.content.Context;

/**
 * Created by yanlu on 16/3/19.
 * Get the weather information from OpenWeatherMap in Json format using its API.
 * Depends on input "OPEN_WEATHER_MAP_API", there are two different API implemented:
 * 1. Get current weather
 * 2. Get daily weather report in the next 10 days
 */

public class GetWeather{


    public static JSONObject getJSON(Context context, String city, String OPEN_WEATHER_MAP_API){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();
            /*The API Key */
            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.weather_api));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(4096);
            String tmp;
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }

}
