package ru.sionyx.meteoradar.tasks;

import org.json.JSONObject;

import ru.sionyx.meteoradar.Urls;

/**
 * Created by vadimbalasov on 30.03.16.
 */
public class DownloadUrlsTask extends DownloadJsonTask {
    DownloadUrlsTaskDelegate _delegate;

    public DownloadUrlsTask(DownloadUrlsTaskDelegate delegate) {
        _delegate = delegate;
    }

    protected void onPostExecute(String result) {
        if (result == null) {
            _delegate.urlsNotLoaded("Request error");
            return;
        }

        Urls urls = null;

        try {
            JSONObject settingsJson = new JSONObject(result);
            urls = new Urls();
            urls.authUrl = settingsJson.getString("auth");
            urls.mapsUrl = settingsJson.getString("maps");
            urls.promoCodeUrl = settingsJson.getString("promocode");
        } catch (Exception e) {
        } finally {
            if (urls != null && urls.authUrl != null && urls.mapsUrl != null && urls.promoCodeUrl != null) {
                _delegate.urlsLoaded(urls);
            }
            else {
                _delegate.urlsNotLoaded("urls parse error");
            }
        }
    }
}
