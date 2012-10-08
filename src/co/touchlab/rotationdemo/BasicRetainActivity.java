package co.touchlab.rotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import co.touchlab.rotationdemo.tasks.DialogRetainedTask;
import co.touchlab.rotationdemo.utils.Weather;

/**
 * User: William Sanville
 * Date: 10/8/12
 * Time: 11:14 AM
 * Demo Activity for using the base class.
 */
public class BasicRetainActivity extends BaseActivity
{
    private static String TAG = "BasicRetainActivity";
    private static String WEATHER_TASK = "WEATHER_TASK";

    private TextView resultText;
    private CharSequence text;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic);

        final EditText editText = (EditText)findViewById(R.id.zip);
        findViewById(R.id.lookup).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String zip = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(zip))
                {
                    LoadWeatherTask task = new LoadWeatherTask(BasicRetainActivity.this, zip);
                    task.execute();
                    addRetainedTask(WEATHER_TASK, task);
                }
            }
        });

        resultText = (TextView)findViewById(R.id.results);

        //Check for additional data which may have been retained along with running tasks.
        Object last = getLastAdditionalNonConfigurationInstance();
        if (last instanceof CharSequence)
        {
            text = (CharSequence)last;
            resultText.setText(text);
        }
    }

    private void onWeatherLoaded(Weather.WeatherInfo info)
    {
        //Tell the base class that we're done
        clearRetainedTask(WEATHER_TASK);

        if (info != null)
        {
            text = Html.fromHtml(String.format("Current temperature for %s is %.0f&deg;F", info.getLocationName(), info.getTemperature()));
        }
        else
        {
            text = "";
            Toast.makeText(this, R.string.unable_to_load_weather, Toast.LENGTH_LONG).show();
        }
        resultText.setText(text);
    }

    /**
     * This is a slightly modified callback used by the base class. Pass through any data like we normally would in
     * onRetainCustomNonConfigurationInstance(), and this will get retained along with any running tasks.
     * @return We'll just return the formatted result text.
     */
    @Override
    public Object onRetainAdditionalNonConfigurationInstance()
    {
        return text;
    }

    /**
     * This class should either be a totally separate class, or, if an inner class of the Activity, it should be marked
     * as STATIC! This is important, because non-static inner classes always have a reference to their outer class,
     * which would be an activity.
     * <p/>
     * Here, we only use the Activity passed in to the onPostExecute() method. In doInBackground(), if we need a Context,
     * we should use getApplicationContext().
     */
    static class LoadWeatherTask extends DialogRetainedTask<Void, Void, Weather.WeatherInfo>
    {
        private String zip;

        LoadWeatherTask(Activity activity, String zip)
        {
            super(activity, R.string.loading_weather);
            this.zip = zip;
        }

        @Override
        protected Weather.WeatherInfo doInBackground(Void... voids)
        {
            try
            {
                return Weather.weatherByZipCode(zip);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Unable to load weather.", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Weather.WeatherInfo weatherInfo, Activity activity)
        {
            super.onPostExecute(weatherInfo, activity);
            if (activity instanceof BasicRetainActivity)
            {
                BasicRetainActivity a = (BasicRetainActivity)activity;
                a.onWeatherLoaded(weatherInfo);
            }
        }
    }
}