package co.touchlab.rotationdemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.touchlab.rotationdemo.utils.Weather;

/**
 * User: William Sanville
 * Date: 10/8/12
 * Time: 12:15 PM
 * An activity that uses the Loader framework for retaining data on a configuration change.
 */
public class LoaderActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Weather.WeatherInfo>
{
    private TextView resultText;
    private String zipInProgress;
    private Dialog dialog;

    private WeatherLoader loader;

    private final static String PROGRESS_KEY = "PROGRESS_KEY";

    private final static int WEATHER_REQUEST_CODE = 1;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic);

        //This always needs to be called, due to how the LoaderManager retains things.
        loader = (WeatherLoader)getSupportLoaderManager().initLoader(WEATHER_REQUEST_CODE, null, this);

        final EditText editText = (EditText)findViewById(R.id.zip);
        findViewById(R.id.lookup).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String zip = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(zip))
                {
                    startLoader(zip);
                }
            }
        });

        resultText = (TextView)findViewById(R.id.results);
        if (savedInstanceState != null)
            zipInProgress = savedInstanceState.getString(PROGRESS_KEY);
    }

    @Override
    public Loader<Weather.WeatherInfo> onCreateLoader(int i, Bundle bundle)
    {
        String zip = bundle != null ? bundle.getString("zip") : null;
        return new WeatherLoader(getApplicationContext(), zip);
    }

    @Override
    public void onLoadFinished(Loader<Weather.WeatherInfo> weatherInfoLoader, Weather.WeatherInfo weatherInfo)
    {
        onWeatherLoaded(weatherInfo);
    }

    @Override
    public void onLoaderReset(Loader<Weather.WeatherInfo> weatherInfoLoader)
    {

    }

    private void onWeatherLoaded(Weather.WeatherInfo info)
    {
        Log.d(getClass().getSimpleName(), "Got response from Loader");

        CharSequence text;
        if (info != null)
            text = info.summary();
        else
            text = getString(R.string.unable_to_load_weather);

        resultText.setText(text);

        if (info == null || info.getZip().equals(zipInProgress))
        {
            zipInProgress = null;
            dismissDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(PROGRESS_KEY, zipInProgress);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        dismissDialog();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (zipInProgress != null)
        {
            showDialog();
        }
    }

    private void showDialog()
    {
        dialog = ProgressDialog.show(this, null, getString(R.string.loading_weather), true, true);
    }

    private void dismissDialog()
    {
        if (dialog != null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void startLoader(String zip)
    {
        showDialog();
        zipInProgress = zip;

        Log.d(getClass().getSimpleName(), "Calling startLoading()");

        loader.setZip(zip);
        loader.startLoading();
    }

    static class WeatherLoader extends AsyncTaskLoader<Weather.WeatherInfo>
    {
        private Weather.WeatherInfo cache;
        private String zip;

        WeatherLoader(Context context, String zip)
        {
            super(context);
            this.zip = zip;
        }

        public String getZip()
        {
            return zip;
        }

        public void setZip(String zip)
        {
            this.zip = zip;
        }

        @Override
        protected void onStartLoading()
        {
            Log.d(LoaderActivity.class.getSimpleName(), "WeatherLoader onStartLoading()");
            if (zip == null)
            {
                Log.d(LoaderActivity.class.getSimpleName(), "No zip set, nothing to do.");
                return;
            }

            if (cache == null || !cache.getZip().equals(zip))
            {
                Log.d(LoaderActivity.class.getSimpleName(), "Cache is stale, calling forceLoad()");
                forceLoad();
            }
        }

        @Override
        public Weather.WeatherInfo loadInBackground()
        {
            Log.d(LoaderActivity.class.getSimpleName(), "WeatherLoader loadInBackground()");
            try
            {
                return Weather.weatherByZipCode(zip);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        public void deliverResult(Weather.WeatherInfo data)
        {
            cache = data;
            super.deliverResult(data);
        }
    }
}
