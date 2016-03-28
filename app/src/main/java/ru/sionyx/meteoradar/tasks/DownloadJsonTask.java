package ru.sionyx.meteoradar.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vadimbalasov on 02.03.16.
 */
public class DownloadJsonTask extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... urls) {
        String settingsUrl = urls[0];
        String jsonString = null;

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
            jsonString = sb.toString();
            is.close();

            // remove any number of BOMs
            jsonString = jsonString.replace("\uFEFF", "");

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        return jsonString;
    }
}
