package co.touchlab.rotationdemo.tasks;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 * User: William Sanville
 * Date: 10/5/12
 * Time: 11:50 AM
 * Base class for showing/hiding a dialog while a retained AsyncTask is running.
 *
 * Always call the super() methods when inheriting from this class!
 */
public abstract class DialogRetainedTask<Params, Progress, Result> extends RetainedTask<Params, Progress, Result>
{
    private int messageResourceId;
    private ProgressDialog dialog;

    protected DialogRetainedTask(Activity activity, int messageResourceId)
    {
        super(activity);
        this.messageResourceId = messageResourceId;
    }

    @Override
    protected void onPreExecute(Activity activity)
    {
        showDialog(activity);
    }

    @Override
    protected void onPostExecute(Result result, Activity activity)
    {
        dismissDialog();
    }

    @Override
    public void attach(Activity activity)
    {
        super.attach(activity);
        if (!isComplete())
            showDialog(activity);
    }

    @Override
    public void detach()
    {
        dismissDialog();
        super.detach();
    }

    private void showDialog(Activity activity)
    {
        dialog = ProgressDialog.show(activity, null, activity.getString(messageResourceId), true, false);
    }

    private void dismissDialog()
    {
        if (dialog != null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }
}

