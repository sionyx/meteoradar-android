package ru.sionyx.meteoradar;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.HashMap;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    FloatingActionButton refreshButton;
    Toolbar toolbar;
    ProgressBar progressBar;
    ImageViewTouch radarZoomable;

    Radar[] radars;
    int currentRadar;
    RadarInfo radarInfo;
    HashMap<String, Bitmap> imagesCache;
    int animationFrame;

    Handler timerHandler;
    Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        radarZoomable = (ImageViewTouch)findViewById(R.id.radar_zoomable);
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

        imagesCache = new HashMap<>();

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int delay = animateFrame();
                timerHandler.postDelayed(this, delay * 500);
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String message = getResources().getString(R.string.loading_radars);
        progressDialog.setMessage(message);
        progressDialog.show();

        DownloadSettingsTask downloadSettingsTask = new DownloadSettingsTask(this, menu);
        downloadSettingsTask.execute("http://meteo.bcr.by/settings.json");

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
            reloadImage(radars[currentRadar]);
        }
        return true;
    }

    public void onSettingsLoaded(Radar[] radars) {
        progressDialog.hide();
        this.radars = radars;

        if (radars.length > 0) {
            currentRadar = 0;
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
        for(int i = 0; i < radarInfo.Maps.length; i++) {
            if (!imagesCache.containsKey(radarInfo.Maps[i].File)) {
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
        if (radarInfo.Maps.length == 0) {
            progressDialog.hide();
            Snackbar.make(radarZoomable, R.string.radar_info_empty, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        this.radarInfo = radarInfo;

        boolean started = false;
        for(int i = 0; i < radarInfo.Maps.length; i++) {
            if (!imagesCache.containsKey(radarInfo.Maps[i].File)) {
                String link = String.format("http://meteo.bcr.by/%s/%s", radar.code, radarInfo.Maps[i].File);

                DownloadMapTask downloadMapTask = new DownloadMapTask(this, radarInfo.Maps[i].File);
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
        progressBar.setMax(radarInfo.Maps.length);
        timerHandler.post(timerRunnable);
    }

    private void stopAnimation() {
        timerHandler.removeCallbacks(timerRunnable);
        progressBar.setVisibility(View.GONE);
    }

    private int animateFrame() {
        progressBar.setProgress(animationFrame + 1);
        String file = radarInfo.Maps[animationFrame].File;
        Bitmap img = imagesCache.get(file);
        if (img != null) {
            Matrix matrix = radarZoomable.getDisplayMatrix();
            radarZoomable.setImageBitmap(img, matrix, 1, 2);
        }

        animationFrame++;
        if (animationFrame == radarInfo.Maps.length) {
            animationFrame = 0;
            return 20;
        }

        return 1;
    }
}


