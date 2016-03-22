# Weather-Android
# Homework Achievement Checklist

*To build an Android application that will display the 10 day forecast retrieved from an API. *
1.	Pull down forecast details from an API (ex: WeatherUnderground, Forecast.io). Make sure that you have the ability to refresh the data. 

I pull down forecast detail from OpenWeatherMap API in JSON format. By clicking the main weather icon ImageButton, the app will refresh the current weather data. And the weather information for the next 10 days will be updated whenever the app is restarted (updated in onCreateView Stage of WeatherFragment)
Note: even after update weather data, the weather information shown on screen may not change, this is because the weather information form API do not change frequently.

2.	Load the forecast into a ViewPager (Android). Each cell should have a background image with an icon displaying what the weather conditions will be on that day along with the temperature on top. 

The UI looks as follow:
    
3.	When a cell (or day) is tapped, a new view should appear that contains the full forecast details for that day. 

The right Figure above shows the full forecast details for any day tapped in the ListView of the first Activity.

4.	Cache the data in sqlite. 

All 10-day forecast data are stored in SQLite by class SQLite. And when showing full forecast details for that day, the data is loaded directly from SQLite.
5.	When tapping on a cell, create a custom transition animation to appear while the
forecast details come into view. 

The transition animation is customized when transit from the left activity to right activity.

6.	Have the forecast be based on a user's geolocation 

When running on a real phone, the app will use the phone location to get weather information. While because the emulator does not support location, the app will use the default (New York) location to get weather from API.
