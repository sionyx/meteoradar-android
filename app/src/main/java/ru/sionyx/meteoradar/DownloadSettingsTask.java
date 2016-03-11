package ru.sionyx.meteoradar;

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

/**
 * Created by vadimbalasov on 01.03.16.
 */

public class DownloadSettingsTask extends DownloadJsonTask {
    MainActivity mainActivity;
    Menu menu;

    public DownloadSettingsTask(MainActivity mainActivity, Menu menu) {
        this.mainActivity = mainActivity;
        this.menu = menu;
    }

    protected void onPostExecute(String result) {
        if (result == null) {
            mainActivity.onSettingsNotLoaded();
        }

        Radar[] radars = null;

        try {
            JSONObject settingsJson = new JSONObject(result);
            JSONArray radarsJson = settingsJson.getJSONArray("radars");

            menu.clear();
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

                    menu.add(0, i, Menu.NONE, radars[i].desc);
                } catch (JSONException e) {
                    // Oops
                }
            }

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        } finally {
            if (radars != null) {
                mainActivity.onSettingsLoaded(radars);
            }
            else {
                mainActivity.onSettingsNotLoaded();
            }
        }

    }
}
