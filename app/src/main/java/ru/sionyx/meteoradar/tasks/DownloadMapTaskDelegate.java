package ru.sionyx.meteoradar.tasks;

import android.graphics.Bitmap;

/**
 * Created by vadimbalasov on 24.03.16.
 */
public interface DownloadMapTaskDelegate {
    void onMapLoaded(Bitmap bitmap, String filename);
    void onMapNotLoaded(String filename);
}
