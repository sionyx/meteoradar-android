package ru.sionyx.meteoradar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vadimbalasov on 02.03.16.
 */
public class DownloadMapTask extends AsyncTask<String, Void, Bitmap> {
    MainActivity mainActivity;
    String filename;

    public DownloadMapTask(MainActivity mainActivity, String filename) {
        this.mainActivity = mainActivity;
        this.filename = filename;
    }

    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap image = null;
        try {
            URL url = new URL(urlDisplay);
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bis.mark(conn.getContentLength());
            image = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            mainActivity.onMapLoaded(result, filename);
        }
        else {
            mainActivity.onMapNotLoaded(filename);
        }
    }
}
