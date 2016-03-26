package com.example.yanlu.weather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by yanlu on 16/3/20.
 * The SQLite class is used to deal with SQLite.
 * The database is used to store the weather data in the next 10 days.
 *
 * It has 2 Override functions:
 * 1. onCreate is called when the app is first run, it builds a SQLite DB to store future weather
 * 2. onUpgrade, when the version is changed, the table will be rebuilt
 *
 * It also has 3 new functions:
 * 1. deleteAll is used to clear data in database before updating the database
 * 2. select is used to get weather data in database by id (Primary Key)
 * 3. insert is used to put the updated data in database
 */

public class SQLite extends SQLiteOpenHelper{
    public SQLite(Context context) {
        super(context, "weather_db", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE FutureWeather (");
        sql.append("id INTEGER PRIMARY KEY,");
        sql.append("Weather TEXT, Description TEXT,");
        sql.append("High TEXT, Low TEXT, Icon TEXT,");
        sql.append("Pressure TEXT, Windspeed TEXT, Humidity TEXT);");
        db.execSQL(sql.toString());
        Log.d("status", "done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP table if EXISTS FutureWeather";
        db.execSQL(sql);
        onCreate(db);
    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("FutureWeather",null,null);
//        for (int i = 1; i<11;i++){
//            db.delete("FutureWeather", "id=?", new String[]{Integer.toString(i)});
//        }
    }

    public Cursor select(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] col = {"*"};
        String[] selArg = {Integer.toString(id)};
        Cursor cur = db.query("FutureWeather",col, "id=?", selArg,null,null,null);
//        Cursor cur = db.rawQuery("select * from FutureWeather where id="+id+"", null);
        return cur;
    }

//    public long insert(JSONObject weather){
    public boolean insert(String weather, String description, String high, String low, String icon, String pressure, String speed, String humidity){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Weather",weather);
        cv.put("Description",description);
        cv.put("High", high);
        cv.put("Low", low);
        cv.put("Icon", icon);
        cv.put("Pressure",pressure);
        cv.put("Windspeed", speed);
        cv.put("Humidity", humidity);
        db.insert("FutureWeather", null, cv);
        return true;
    }
}
