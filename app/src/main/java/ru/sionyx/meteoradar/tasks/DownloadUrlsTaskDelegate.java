package ru.sionyx.meteoradar.tasks;

import ru.sionyx.meteoradar.Urls;

/**
 * Created by vadimbalasov on 30.03.16.
 */
public interface DownloadUrlsTaskDelegate {
    void urlsLoaded(Urls urls);
    void urlsNotLoaded(String error);
}
