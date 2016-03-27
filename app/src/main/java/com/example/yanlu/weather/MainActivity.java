package com.example.yanlu.weather;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by yanlu on 16/3/19.
 * This is the MainActivity of the program,
 * A Fragment called WeatherFragment is built on it
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WeatherFragment())
                    .commit();
        }
    }

}
