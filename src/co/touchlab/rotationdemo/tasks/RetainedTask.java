package co.touchlab.rotationdemo.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

/**
 * User: William Sanville
 * Date: 10/5/12
 * Time: 11:39 AM
 * Base class for some functionality for restoring a running task during a screen rotation.
 *
 * This class is basically designed to provide public methods to attach and detach a host activity, and to be sure that
 * a subclass of this can NOT get the Activity from the doInBackground() method, because the activity member variable
 * could be set to null at any time.
 */
public abstract class RetainedTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
    private Activity activity;
    protected Context applicationContext;
    private boolean complete;

    protected RetainedTask(Activity activity)
    {
        this.activity = activity;
        applicationContext = activity.getApplicationContext();
    }

    public void detach()
    {
        activity = null;
    }

    public void attach(Activity activity)
    {
        this.activity = activity;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public Context getApplicationContext()
    {
        return applicationContext;
    }

    @Override
    protected final void onPreExecute()
    {
        onPreExecute(activity);
    }

    protected void onPreExecute(Activity activity)
    {

    }

    @Override
    protected final void onPostExecute(Result result)
    {
        complete = true;
        onPostExecute(result, activity);
    }

    protected void onPostExecute(Result result, Activity activity)
    {

    }
}

