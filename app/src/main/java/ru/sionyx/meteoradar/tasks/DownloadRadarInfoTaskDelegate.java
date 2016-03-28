package ru.sionyx.meteoradar.tasks;

import ru.sionyx.meteoradar.Radar;
import ru.sionyx.meteoradar.RadarInfo;

/**
 * Created by vadimbalasov on 24.03.16.
 */

public interface DownloadRadarInfoTaskDelegate {
    void onRadarInfoLoaded(Radar radar, RadarInfo radarInfo);
    void onRadarInfoNotLoaded(String error);
}
