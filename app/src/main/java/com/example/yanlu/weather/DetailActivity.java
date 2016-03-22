package com.example.yanlu.weather;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yanlu on 16/3/21.
 * This Activity is used to show the detailed weather information in one of the future 10 days
 * The information includes Weather, Weather Description, Weather Icon, Temperature, Humidity, Wind Speed, Pressure
 * All information are loaded from SQLite
 */

public class DetailActivity extends AppCompatActivity {

    private SQLite db;
    TextView detWeather;
    TextView detTemp;
    TextView detDate;
    TextView detDescription;
    TextView detHumidity;
    TextView detSpeed;
    TextView detPressure;
    ImageView detWeatherImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);
        detWeatherImg = (ImageView) findViewById(R.id.imageButtonDetWeather);
        detWeather = (TextView) findViewById(R.id.textViewDetWeather);
        detTemp = (TextView) findViewById(R.id.textViewDetTempAvg);
        detDate = (TextView) findViewById(R.id.textViewDetDate);
        detDescription = (TextView) findViewById(R.id.textViewDetDescription);
        detHumidity = (TextView) findViewById(R.id.textViewDetHumidity);
        detSpeed = (TextView) findViewById(R.id.textViewDetWind);
        detPressure = (TextView) findViewById(R.id.textViewDetPressure);
        db = new SQLite(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            int Row = extras.getInt("id");
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
            calendar.add(Calendar.DAY_OF_YEAR, Row - 1);
            Date rawDay = calendar.getTime();
            Cursor cur = db.select(Row);
            cur.moveToFirst();
            detDate.setText(dayFormat.format(rawDay));
            detWeatherImg.setImageResource((Integer) ImageSelect.weatherMainIcon(cur.getString(5)));
            detWeather.setText(cur.getString(1));
            detDescription.setText(cur.getString(2));
            detTemp.setText(cur.getString(4)+"℉ - "+cur.getString(3)+"℉");
            detPressure.setText("Pressure: "+cur.getString(6)+" hPa");
            detSpeed.setText("WindSpeed: "+cur.getString(7)+" m/s");
            detHumidity.setText("Humidity: "+cur.getString(8)+"%");
            if(!cur.isClosed()){
                cur.close();
            }
        }
    }
}
