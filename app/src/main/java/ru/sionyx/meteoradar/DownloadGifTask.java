package ru.sionyx.meteoradar;

import android.app.ProgressDialog;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vadimbalasov on 01.03.16.
 */

public class DownloadGifTask extends AsyncTask<String, Void, Movie> {
    MainActivity mainActivity;

    public DownloadGifTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    protected Movie doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Movie movie = null;
        try {
            URL url = new URL(urlDisplay);
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bis.mark(conn.getContentLength());
            movie = Movie.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return movie;
    }

    protected void onPostExecute(Movie result) {
//        if (result != null) {
//            mainActivity.onMapLoaded(result);
//        }
//        else {
//            mainActivity.onMapNotLoaded();
//        }
    }
}
