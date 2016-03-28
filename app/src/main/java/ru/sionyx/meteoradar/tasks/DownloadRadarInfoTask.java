package ru.sionyx.meteoradar.tasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.sionyx.meteoradar.Map;
import ru.sionyx.meteoradar.MapSource;
import ru.sionyx.meteoradar.Radar;
import ru.sionyx.meteoradar.RadarInfo;

/**
 * Created by vadimbalasov on 02.03.16.
 */

public class DownloadRadarInfoTask extends DownloadJsonTask {
    DownloadRadarInfoTaskDelegate _delegate;
    Radar _radar;

    public DownloadRadarInfoTask(DownloadRadarInfoTaskDelegate delegate, Radar radar) {
        _delegate = delegate;
        _radar = radar;
    }

    protected void onPostExecute(String result) {
        if (result == null) {
            _delegate.onRadarInfoNotLoaded("request error");
            return;
        }

        if (result.length() > 7 && result.substring(0, 7).equalsIgnoreCase("error: ")) {
            _delegate.onRadarInfoNotLoaded(result.substring(7));
            return;
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
                _delegate.onRadarInfoLoaded(_radar, radarInfo);
            }
            else {
                _delegate.onRadarInfoNotLoaded("JSON parse error");
            }
        }

    }

}
