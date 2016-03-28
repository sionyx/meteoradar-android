package ru.sionyx.meteoradar.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import ru.sionyx.meteoradar.Radar;

/**
 * Created by vadimbalasov on 01.03.16.
 */

public class DownloadSettingsTask extends DownloadJsonTask {
    DownloadSettingsTaskDelegate _delegate;

    public DownloadSettingsTask(DownloadSettingsTaskDelegate delegate) {
        _delegate = delegate;
    }

    protected void onPostExecute(String result) {
        if (result == null) {
            _delegate.onSettingsNotLoaded();
            return;
        }

        Radar[] radars = null;

        try {
            JSONObject settingsJson = new JSONObject(result);
            JSONArray radarsJson = settingsJson.getJSONArray("radars");

            radars = new Radar[radarsJson.length()];

            for (int i=0; i < radarsJson.length(); i++)
            {
                try {
                    JSONObject radarJson = radarsJson.getJSONObject(i);
                    radars[i] = new Radar();
                    // Pulling items from the array
                    radars[i].code = radarJson.getString("code");
                    radars[i].desc = radarJson.getString("desc");
                    radars[i].interval = radarJson.getString("interval");
                } catch (JSONException e) {
                    // Oops
                }
            }

        } catch (Exception e) {
        } finally {
            if (radars != null) {
                _delegate.onSettingsLoaded(radars);
            }
            else {
                _delegate.onSettingsNotLoaded();
            }
        }
    }
}
