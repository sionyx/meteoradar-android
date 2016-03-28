package ru.sionyx.meteoradar.ViewModels;

import android.graphics.Bitmap;
import android.text.Html;

import java.util.HashMap;

import ru.sionyx.meteoradar.Radar;
import ru.sionyx.meteoradar.RadarInfo;
import ru.sionyx.meteoradar.tasks.DownloadMapTask;
import ru.sionyx.meteoradar.tasks.DownloadMapTaskDelegate;
import ru.sionyx.meteoradar.tasks.DownloadRadarInfoTask;
import ru.sionyx.meteoradar.tasks.DownloadRadarInfoTaskDelegate;

/**
 * Created by vadimbalasov on 27.03.16.
 */
public class MapsViewModel implements DownloadMapTaskDelegate, DownloadRadarInfoTaskDelegate {
    static MapsViewModel _shared;
    HashMap<String, Bitmap> imagesCache;

    public MapsViewModelDelegate delegate;
    public RadarInfo radarInfo;

    public static MapsViewModel shared() {
        if (_shared == null) {
            _shared = new MapsViewModel();
            _shared.imagesCache = new HashMap<>();
        }
        return _shared;
    }

    public void loadMapImages(Radar radar) {
        String link = String.format("http://meteo.bcr.by/req2.php?rad=%s", radar.code);

        DownloadRadarInfoTask downloadRadarInfoTask = new DownloadRadarInfoTask(this, radar);
        downloadRadarInfoTask.execute(link);
    }

    public void DropCache() {
        imagesCache.clear();
    }

    public Bitmap bitmapForIndex(int index) {
        String file = radarInfo.maps[index].File;
        Bitmap bitmap = imagesCache.get(file);
        return bitmap;
    }

    //region DownloadRadarInfoTaskDelegate

    public void onRadarInfoLoaded(Radar radar, RadarInfo radarInfo) {
        delegate.radarInfoLoaded(radarInfo);

        this.radarInfo = radarInfo;

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
            delegate.mapsLoaded();
        }
    }

    public void onRadarInfoNotLoaded(String error) {
        delegate.radarInfoNotLoaded(error);
    }

    // endregion

    //region DownloadMapTaskDelegate

    public void onMapLoaded(Bitmap bitmap, String filename) {
        imagesCache.put(filename, bitmap);

        boolean finished = true;
        for (int i = 0; i < radarInfo.maps.length; i++) {
            if (!imagesCache.containsKey(radarInfo.maps[i].File)) {
                finished = false;
                break;
            }
        }

        if (finished) {
            delegate.mapsLoaded();
        }
    }

    public void onMapNotLoaded(String filename) {
        delegate.mapsNotLoaded();
    }

    //endregion
}
