package co.touchlab.rotationdemo;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import co.touchlab.rotationdemo.utils.Weather;

/**
 * User: William Sanville
 * Date: 10/15/12
 * Time: 12:23 PM
 * An IntentService that fetches weather (in a background thread) and sends the result back to the Activity's
 * onActivityResult() method.
 */
public class WeatherIntentService extends IntentService
{
    public final static String ZIP = "ZIP";
    public final static String WEATHER_INFO = "WEATHER_INFO";
    public final static String PENDING_RESULT = "PENDING_RESULT";

    public WeatherIntentService()
    {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String zip = intent.getStringExtra(ZIP);
        PendingIntent pendingResult = intent.getParcelableExtra(PENDING_RESULT);

        Weather.WeatherInfo weatherInfo = null;
        try
        {
            weatherInfo = Weather.weatherByZipCode(zip);
        }
        catch (Exception e)
        {
            Log.e(getClass().getSimpleName(), "Unable to load weather.", e);
        }

        try
        {
            pendingResult.send(this.getApplicationContext(), Activity.RESULT_OK, new Intent().putExtra(WEATHER_INFO, weatherInfo));
            Log.d(getClass().getSimpleName(), "Sent pending intent");
        }
        catch (PendingIntent.CanceledException e)
        {
            Log.e(getClass().getSimpleName(), "Pending intent was canceled", e);
        }
    }
}
