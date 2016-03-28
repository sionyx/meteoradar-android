package ru.sionyx.meteoradar;


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

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import ru.sionyx.meteoradar.ViewModels.MapsViewModel;
import ru.sionyx.meteoradar.ViewModels.MapsViewModelDelegate;
import ru.sionyx.meteoradar.ViewModels.RadarsViewModel;
import ru.sionyx.meteoradar.ViewModels.RadarsViewModelDelegate;


public class MainActivity extends AppCompatActivity
        implements RadarsViewModelDelegate, MapsViewModelDelegate {
    ProgressDialog progressDialog;
    FloatingActionButton refreshButton;
    Toolbar toolbar;
    ProgressBar progressBar;
    ImageViewTouch radarZoomable;
    TextView sourceLink;

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
                if (RadarsViewModel.shared().radars == null) {
                    RadarsViewModel.shared().loadSettings();
                }
                else {
                    reloadImage(RadarsViewModel.shared().radar);
                }
            }
        });

        sourceLink = (TextView)findViewById(R.id.sourceLink);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int delay = animateFrame();
                timerHandler.postDelayed(this, delay * 500);
            }
        };

        // Init View Models
        RadarsViewModel.shared().accountManager = (AccountManager)getSystemService(ACCOUNT_SERVICE);
        RadarsViewModel.shared().delegate = this;
        MapsViewModel.shared().delegate = this;

        // Start
        if (RadarsViewModel.shared().radars == null) {
            RadarsViewModel.shared().loadSettings();
        }
        else if (MapsViewModel.shared().radarInfo == null) {
            reloadImage(RadarsViewModel.shared().radar);
        }
        else {
            mapsLoaded();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (RadarsViewModel.shared().radars != null) {
            for (int i = 0; i < RadarsViewModel.shared().radars.length; i++) {
                menu.add(0, i, Menu.NONE, RadarsViewModel.shared().radars[i].desc);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int index = item.getItemId();

        if (RadarsViewModel.shared().radars != null && index < RadarsViewModel.shared().radars.length) {
            Radar selectedRadar = RadarsViewModel.shared().radars[index];
            if (selectedRadar == RadarsViewModel.shared().radar) {
                return true;
            }

            MapsViewModel.shared().DropCache();
            RadarsViewModel.shared().radar = selectedRadar;
            SaveCurrentRadar(selectedRadar);
            reloadImage(selectedRadar);
        }
        return true;
    }

    //region MapsViewModelDelegate

    public void radarInfoLoaded(RadarInfo radarInfo) {
        if (radarInfo.maps.length == 0) {
            progressDialog.hide();
            Snackbar.make(radarZoomable, R.string.radar_info_empty, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        if (radarInfo.source != null) {
            sourceLink.setText(Html.fromHtml(String.format("Источник: <a href='%s'>%s</a>", radarInfo.source.link, radarInfo.source.title)));
        }
    }

    public void radarInfoNotLoaded(String error) {
        progressDialog.hide();
        String message = String.format("%s: %s", getResources().getString(R.string.cannot_load_radar_info), error);

        Snackbar.make(radarZoomable, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void mapsLoaded() {
        progressDialog.hide();
        startAnimation();
    }

    public void mapsNotLoaded() {
        progressDialog.hide();
        Snackbar.make(radarZoomable, R.string.cannot_load_map, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    //endregion


    //region RadarsViewModelDelegate

    public void radarsStartLoading() {
        String message = getResources().getString(R.string.loading_radars);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void radarsLoaded(Radar[] radars) {
        progressDialog.hide();
        invalidateOptionsMenu();

        if (radars.length > 0) {
            Radar radar = RadarsViewModel.shared().radars[0];

            String currentRadarCode = LoadCurrentRadarCode();
            for (Radar r : radars) {
                if (r.code.equals(currentRadarCode)) {
                    radar = r;
                    break;
                }
            }

            RadarsViewModel.shared().radar = radar;
            reloadImage(radar);
        }
    }

    public void radarsNotLoaded(String error) {
        progressDialog.hide();
        Snackbar.make(radarZoomable, R.string.cannot_load_radars, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void promoStartSending() {

    }

    public void promoSent() {

    }

    public void promoFailedToSend(String error) {

    }

    //endregion


    private void reloadImage(Radar radar) {
        stopAnimation();
        radarZoomable.setImageBitmap(null);
        toolbar.setTitle(radar.desc);
        String message = getResources().getString(R.string.loading_map);
        progressDialog.setMessage(message);
        progressDialog.show();

        MapsViewModel.shared().loadMapImages(radar);
    }



    private void startAnimation() {
        animationFrame = 0;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(MapsViewModel.shared().radarInfo.maps.length);
        timerHandler.post(timerRunnable);
    }

    private void stopAnimation() {
        timerHandler.removeCallbacks(timerRunnable);
        progressBar.setVisibility(View.GONE);
    }

    private int animateFrame() {
        progressBar.setProgress(animationFrame + 1);
        Bitmap img = MapsViewModel.shared().bitmapForIndex(animationFrame);
        if (img != null) {
            Matrix matrix = radarZoomable.getDisplayMatrix();
            radarZoomable.setImageBitmap(img, matrix, 1, 2);
        }

        animationFrame++;
        if (animationFrame == MapsViewModel.shared().radarInfo.maps.length) {
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


