package ru.sionyx.meteoradar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    FloatingActionButton refreshButton;
    Toolbar toolbar;
    ProgressBar progressBar;
    ImageViewTouch radarZoomable;
    TextView sourceLink;

    Radar[] radars;
    int currentRadar;
    RadarInfo radarInfo;
    HashMap<String, Bitmap> imagesCache;
    int animationFrame;

    Handler timerHandler;
    Runnable timerRunnable;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        radarZoomable = (ImageViewTouch) findViewById(R.id.radar_zoomable);
        radarZoomable.setDisplayType(ImageViewTouchBase.DisplayType.NONE);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        refreshButton = (FloatingActionButton) findViewById(R.id.fab);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadImage(radars[currentRadar]);
            }
        });

        sourceLink = (TextView)findViewById(R.id.sourceLink);

        imagesCache = new HashMap<>();

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int delay = animateFrame();
                timerHandler.postDelayed(this, delay * 500);
            }
        };

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String message = getResources().getString(R.string.loading_radars);
        progressDialog.setMessage(message);
        progressDialog.show();

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String accountHash = (list.length > 0) ? hash.sha1(list[0].name) : "unknown";

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        String sign = String.format("meteo%02d%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        String signHash = hash.sha1(sign);

        String url = String.format("http://meteo.bcr.by/auth.php?hash=%s&id=%s", accountHash, signHash);

        DownloadSettingsTask downloadSettingsTask = new DownloadSettingsTask(this, menu);
        downloadSettingsTask.execute(url);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentRadar == item.getItemId()) {
            return true;
        }

        imagesCache.clear();
        currentRadar = item.getItemId();
        if (radars != null && currentRadar < radars.length) {
            SaveCurrentRadar(radars[currentRadar]);
            reloadImage(radars[currentRadar]);
        }
        return true;
    }

    public void onSettingsLoaded(Radar[] radars) {
        progressDialog.hide();
        this.radars = radars;

        if (radars.length > 0) {
            currentRadar = 0;

            String currentRadarCode = LoadCurrentRadarCode();
            for (int i = 0; i < radars.length; i++) {
                if (radars[i].code.equals(currentRadarCode)) {
                    currentRadar = i;
                    break;
                }
            }

            reloadImage(radars[currentRadar]);
        }
    }

    public void onSettingsNotLoaded() {
        progressDialog.hide();
        Snackbar.make(radarZoomable, R.string.cannot_load_radars, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        refreshButton.setEnabled(false);
    }


    private void reloadImage(Radar radar) {
        stopAnimation();
        radarZoomable.setImageBitmap(null);
        toolbar.setTitle(radar.desc);
        String message = getResources().getString(R.string.loading_map);
        progressDialog.setMessage(message);
        progressDialog.show();

        String link = String.format("http://meteo.bcr.by/req2.php?rad=%s", radar.code);

        DownloadRadarInfoTask downloadRadarInfoTask = new DownloadRadarInfoTask(this, radar);
        downloadRadarInfoTask.execute(link);
    }

    public void onMapLoaded(Bitmap image, String filename) {
        imagesCache.put(filename, image);

        boolean finished = true;
        for (int i = 0; i < radarInfo.maps.length; i++) {
            if (!imagesCache.containsKey(radarInfo.maps[i].File)) {
                finished = false;
                break;
            }
        }

        if (finished) {
            progressDialog.hide();
            startAnimation();
        }
    }

    public void onMapNotLoaded(String filename) {
        progressDialog.hide();
        Snackbar.make(radarZoomable, R.string.cannot_load_map, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onRadarInfoLoaded(Radar radar, RadarInfo radarInfo) {
        if (radarInfo.maps.length == 0) {
            progressDialog.hide();
            Snackbar.make(radarZoomable, R.string.radar_info_empty, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        this.radarInfo = radarInfo;

        if (radarInfo.source != null) {
            sourceLink.setText(Html.fromHtml(String.format("Источник: <a href='%s'>%s</a>", radarInfo.source.link, radarInfo.source.title)));
        }

        boolean started = false;
        for (int i = 0; i < radarInfo.maps.length; i++) {
            if (!imagesCache.containsKey(radarInfo.maps[i].File)) {
                String link = String.format("http://meteo.bcr.by/%s/%s", radar.code, radarInfo.maps[i].File);

                DownloadMapTask downloadMapTask = new DownloadMapTask(this, radarInfo.maps[i].File);
                downloadMapTask.execute(link);

                started = true;
            }
        }

        if (!started) {
            progressDialog.hide();
            startAnimation();
        }
    }

    public void onRadarInfoNotLoaded() {
        progressDialog.hide();
        Snackbar.make(radarZoomable, R.string.cannot_load_radar_info, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void startAnimation() {
        animationFrame = 0;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(radarInfo.maps.length);
        timerHandler.post(timerRunnable);
    }

    private void stopAnimation() {
        timerHandler.removeCallbacks(timerRunnable);
        progressBar.setVisibility(View.GONE);
    }

    private int animateFrame() {
        progressBar.setProgress(animationFrame + 1);
        String file = radarInfo.maps[animationFrame].File;
        Bitmap img = imagesCache.get(file);
        if (img != null) {
            Matrix matrix = radarZoomable.getDisplayMatrix();
            radarZoomable.setImageBitmap(img, matrix, 1, 2);
        }

        animationFrame++;
        if (animationFrame == radarInfo.maps.length) {
            animationFrame = 0;
            return 20;
        }

        return 1;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ru.sionyx.meteoradar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ru.sionyx.meteoradar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void SaveCurrentRadar(Radar radar) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_radar), radar.code);
        editor.commit();
    }

    public String LoadCurrentRadarCode() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String code = sharedPref.getString(getString(R.string.saved_radar), "");
        return code;
    }
}


