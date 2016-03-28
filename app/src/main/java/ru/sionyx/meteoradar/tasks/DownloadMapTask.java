package ru.sionyx.meteoradar.tasks;

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
    DownloadMapTaskDelegate _delegate;
    String _filename;

    public DownloadMapTask(DownloadMapTaskDelegate delegate, String filename) {
        _delegate = delegate;
        _filename = filename;
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
            _delegate.onMapLoaded(result, _filename);
        }
        else {
            _delegate.onMapNotLoaded(_filename);
        }
    }
}
