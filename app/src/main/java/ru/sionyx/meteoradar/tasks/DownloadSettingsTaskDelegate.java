package ru.sionyx.meteoradar.tasks;

import ru.sionyx.meteoradar.Radar;

/**
 * Created by vadimbalasov on 24.03.16.
 */

public interface DownloadSettingsTaskDelegate {
    void onSettingsLoaded(Radar[] radars);
    void onSettingsNotLoaded();
}
