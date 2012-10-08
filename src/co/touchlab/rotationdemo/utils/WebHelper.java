package co.touchlab.rotationdemo.utils;

import android.os.Build;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * User: William Sanville
 * Date: 10/8/12
 * Time: 11:52 AM
 * Simple helper class for making HTTP requests.
 */
public class WebHelper
{
    private static final String TAG = "WebHelper";

    private WebHelper()
    {
    }

    public static String performGet(String urlString) throws IOException
    {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        ByteArrayOutputStream out = null;
        InputStream in = null;

        try
        {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK)
                throw new IOException("Expected HTTP 200, got status code " + statusCode);

            in = new BufferedInputStream(urlConnection.getInputStream());
            out = new ByteArrayOutputStream();

            int b;
            while ((b = in.read()) != -1)
            {
                out.write(b);
            }

            return out.toString();
        }
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (final IOException e)
                {
                    Log.e(TAG, "Error in performGet." + e);
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Error in performGet." + e);
                }
            }
        }
    }

    private static void disableConnectionReuseIfNecessary()
    {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug())
        {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static boolean hasHttpConnectionBug()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }
}
