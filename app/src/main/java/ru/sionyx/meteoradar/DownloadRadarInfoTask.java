package ru.sionyx.meteoradar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vadimbalasov on 02.03.16.
 */

public class DownloadRadarInfoTask extends DownloadJsonTask {
    MainActivity mainActivity;
    Radar radar;

    public DownloadRadarInfoTask(MainActivity mainActivity, Radar radar) {
        this.mainActivity = mainActivity;
        this.radar = radar;
    }

    protected void onPostExecute(String result) {
        if (result == null) {
            mainActivity.onRadarInfoNotLoaded();
        }

        RadarInfo radarInfo = null;

        try {
            JSONObject radarInfoJson = new JSONObject(result);
            JSONArray mapsJson = radarInfoJson.getJSONArray("maps");

            radarInfo = new RadarInfo();
            radarInfo.maps = new Map[mapsJson.length()];

            for (int i=0; i < mapsJson.length(); i++)
            {
                try {
                    JSONObject radarJson = mapsJson.getJSONObject(i);
                    radarInfo.maps[i] = new Map();
                    // Pulling items from the array
                    radarInfo.maps[i].File = radarJson.getString("file");

                } catch (JSONException e) {
                    radarInfo = null;
                    break;
                }
            }

            try {
                JSONObject sourceJson = radarInfoJson.getJSONObject("source");
                if (sourceJson != null) {
                    radarInfo.source = new MapSource();
                    radarInfo.source.title = sourceJson.getString("title");
                    radarInfo.source.link = sourceJson.getString("url");
                }
            }
            catch (Exception e) {
                radarInfo.source = null;
            }

        } catch (Exception e) {
            radarInfo = null;
        } finally {
            if (radarInfo != null) {
                mainActivity.onRadarInfoLoaded(radar, radarInfo);
            }
            else {
                mainActivity.onRadarInfoNotLoaded();
            }
        }

    }

}
