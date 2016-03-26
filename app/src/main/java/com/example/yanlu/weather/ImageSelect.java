package com.example.yanlu.weather;

/**
 * Created by yanlu on 16/3/20.
 * The ImageSelect class is used to transfer icon id to image
 * There are in total 18 different icon statuses to support different weather at day/night
 * There are two functions:
 * 1.weatherIcon returns the small icon for future weather
 * 2.weatherMainIcon returns the large icon for the main weather icon in a page
 */

public class ImageSelect {
    public static Integer weatherIcon(String icon){
        int img;
        switch(icon){
            case "01d": img = R.drawable.d01;
                break;
            case "02d": img = R.drawable.d02;
                break;
            case "03d": img = R.drawable.d03;
                break;
            case "04d": img = R.drawable.d04;
                break;
            case "09d": img = R.drawable.d09;
                break;
            case "10d": img = R.drawable.d10;
                break;
            case "11d": img = R.drawable.d11;
                break;
            case "13d": img = R.drawable.d13;
                break;
            case "50d": img = R.drawable.d50;
                break;
            case "01n": img = R.drawable.n01;
                break;
            case "02n": img = R.drawable.n02;
                break;
            case "03n": img = R.drawable.n03;
                break;
            case "04n": img = R.drawable.n04;
                break;
            case "09n": img = R.drawable.n09;
                break;
            case "10n": img = R.drawable.n10;
                break;
            case "11n": img = R.drawable.n11;
                break;
            case "13n": img = R.drawable.n13;
                break;
            case "50n": img = R.drawable.n50;
                break;
            default: img = R.drawable.list;
                break;

        }
        return img;
    }

    public static Integer weatherMainIcon(String icon){
        int img;
        switch(icon){
            case "01d": img = R.mipmap.dd1;
                break;
            case "02d": img = R.mipmap.dd02;
                break;
            case "03d": img = R.mipmap.dd03;
                break;
            case "04d": img = R.mipmap.dd04;
                break;
            case "09d": img = R.mipmap.dd09;
                break;
            case "10d": img = R.mipmap.dd10;
                break;
            case "11d": img = R.mipmap.dd11;
                break;
            case "13d": img = R.mipmap.dd13;
                break;
            case "50d": img = R.mipmap.dd50;
                break;
            case "01n": img = R.mipmap.nn01;
                break;
            case "02n": img = R.mipmap.nn02;
                break;
            case "03n": img = R.mipmap.nn03;
                break;
            case "04n": img = R.mipmap.dd04;
                break;
            case "09n": img = R.mipmap.dd09;
                break;
            case "10n": img = R.mipmap.nn10;
                break;
            case "11n": img = R.mipmap.nn11;
                break;
            case "13n": img = R.mipmap.dd13;
                break;
            case "50n": img = R.mipmap.dd50;
                break;
            default: img = R.mipmap.list;
                break;

        }
        return img;
    }
}
