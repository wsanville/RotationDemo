package co.touchlab.rotationdemo;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.touchlab.rotationdemo.utils.Weather;

/**
 * User: William Sanville
 * Date: 10/15/12
 * Time: 12:21 PM
 * A reimplementation of BasicRetainActivity using an IntentService.
 */
public class ServiceActivity extends FragmentActivity
{
    private TextView resultText;
    private CharSequence text;
    private boolean workInProgress;
    private Dialog dialog;

    private final static String TEXT_KEY = "TEXT_KEY";
    private final static String PROGRESS_KEY = "PROGRESS_KEY";

    //Used for the result sent by the IntentService
    private final static int WEATHER_REQUEST_CODE = 1;

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
                    startWeatherService(zip);
                }
            }
        });

        resultText = (TextView)findViewById(R.id.results);
        if (savedInstanceState != null)
        {
            if (savedInstanceState.containsKey(TEXT_KEY))
            {
                text = savedInstanceState.getCharSequence(TEXT_KEY);
                resultText.setText(text);
            }
            workInProgress = savedInstanceState.getBoolean(PROGRESS_KEY, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == WEATHER_REQUEST_CODE)
            {
                Log.d(WeatherIntentService.class.getSimpleName(), "Got response from WeatherIntentService");

                Weather.WeatherInfo info = (Weather.WeatherInfo)data.getSerializableExtra(WeatherIntentService.WEATHER_INFO);
                if (info != null)
                    text = info.summary();
                else
                    text = getString(R.string.unable_to_load_weather);
                resultText.setText(text);

                workInProgress = false;
                dismissDialog();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TEXT_KEY, text);
        outState.putBoolean(PROGRESS_KEY, workInProgress);
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
        if (workInProgress)
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

    private void startWeatherService(String zip)
    {
        showDialog();
        workInProgress = true;

        PendingIntent pendingResult = createPendingResult(WEATHER_REQUEST_CODE, new Intent(this, ServiceActivity.class), 0);

        Intent intent = new Intent(this, WeatherIntentService.class)
                .putExtra(WeatherIntentService.ZIP, zip)
                .putExtra(WeatherIntentService.PENDING_RESULT, pendingResult);
        startService(intent);
    }
}
