package co.touchlab.rotationdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import co.touchlab.rotationdemo.tasks.RetainedTask;

import java.util.HashMap;
import java.util.Map;

/**
 * User: William Sanville
 * Date: 10/8/12
 * Time: 10:58 AM
 * A base Activity to inherit from for all other Activities. This is used to register running AsyncTasks so they can be
 * passed from destroyed activities to new instances upon a configuration change.
 * <p/>
 * If needed, this can be changed to extend something else like the native Activity class, SherlockFragmentActivity,
 * whatever.
 */
public abstract class BaseActivity extends FragmentActivity
{
    private Map<String, RetainedTask> retainedTasks = new HashMap<String, RetainedTask>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Here we will process any retained tasks, and attach the current instance of the Activity.
        checkForRetainedTasks();
    }

    private void checkForRetainedTasks()
    {
        Object last = getLastCustomNonConfigurationInstance();
        if (last instanceof RetainPayload)
        {
            retainedTasks = ((RetainPayload)last).retainedTasks;

            for (RetainedTask task : retainedTasks.values())
                task.attach(this);
        }
    }

    protected void addRetainedTask(String key, RetainedTask task)
    {
        RetainedTask existing = retainedTasks.get(key);
        if (existing != null)
            existing.detach();

        retainedTasks.put(key, task);
    }

    protected boolean hasRetainedTask(String key)
    {
        return retainedTasks.containsKey(key);
    }

    protected void clearRetainedTask(String key)
    {
        RetainedTask removed = retainedTasks.remove(key);
        if (removed != null)
            removed.detach();
    }

    /**
     * Used to pass any running AsyncTasks through to a new version of this activity
     *
     * @return A map of running tasks.
     */
    @Override
    public final Object onRetainCustomNonConfigurationInstance()
    {
        for (RetainedTask task : retainedTasks.values())
            task.detach();

        return new RetainPayload(retainedTasks, onRetainAdditionalNonConfigurationInstance());
    }

    /**
     * Sub classes can override this to pass any info they would need, aside from running AsyncTasks to a new instance.
     *
     * @return Some object to retain, just like you would return in onRetainCustomNonConfigurationInstance().
     */
    public Object onRetainAdditionalNonConfigurationInstance()
    {
        return null;
    }

    /**
     * Sub classes can override this to get the result of onRetainAdditionalNonConfigurationInstance() on a destroyed
     * instance.
     *
     * @return Some object that was retained via onRetainAdditionalNonConfigurationInstance().
     */
    public Object getLastAdditionalNonConfigurationInstance()
    {
        Object last = getLastCustomNonConfigurationInstance();
        if (last == null)
            return null;
        else if (last instanceof RetainPayload)
            return ((RetainPayload)last).additional;
        else
            throw new ClassCastException("Unable to retrieve additional retained data, type passed through via onRetainCustomNonConfigurationInstance() was not of type RetainPayload.");
    }

    private static class RetainPayload
    {
        Map<String, RetainedTask> retainedTasks;
        Object additional;

        private RetainPayload(Map<String, RetainedTask> retainedTasks, Object additional)
        {
            this.additional = additional;
            this.retainedTasks = retainedTasks;
        }
    }
}
