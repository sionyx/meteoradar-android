package ru.sionyx.meteoradar.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vadimbalasov on 29.03.16.
 */
public class SendPromoCodeTask extends AsyncTask<String, Void, String> {
    SendPromoCodeTaskDelegate _delegate;

    public SendPromoCodeTask(SendPromoCodeTaskDelegate delegate) {
        _delegate = delegate;
    }

    protected String doInBackground(String... urls) {
        String settingsUrl = urls[0];
        String response = null;

        try {
            URL url = new URL(settingsUrl);
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            response = sb.toString();
            is.close();

            // remove any number of BOMs
            response = response.replace("\uFEFF", "");

        } catch (Exception e) {
        }

        return response;

    }

    protected void onPostExecute(String result) {
        if (result == null) {
            _delegate.promoCodeNotSent("request error");
        }

        if (result.length() > 7 && result.substring(0, 7).equalsIgnoreCase("error: ")) {
            _delegate.promoCodeNotSent(result.substring(7));
        }
        else {
            _delegate.promoCodeSent();
        }
    }
}