package ru.sionyx.meteoradar.ViewModels;

import ru.sionyx.meteoradar.RadarInfo;

/**
 * Created by vadimbalasov on 27.03.16.
 */
public interface MapsViewModelDelegate {
    void radarInfoLoaded(RadarInfo radarInfo);
    void radarInfoNotLoaded(String error);
    void mapsLoaded();
    void mapsNotLoaded();
}
