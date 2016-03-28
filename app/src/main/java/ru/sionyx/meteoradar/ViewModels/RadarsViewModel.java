package ru.sionyx.meteoradar.ViewModels;

import android.accounts.Account;
import android.accounts.AccountManager;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

import ru.sionyx.meteoradar.Radar;
import ru.sionyx.meteoradar.hash;
import ru.sionyx.meteoradar.tasks.DownloadSettingsTask;
import ru.sionyx.meteoradar.tasks.DownloadSettingsTaskDelegate;
import ru.sionyx.meteoradar.tasks.SendPromoCodeTask;
import ru.sionyx.meteoradar.tasks.SendPromoCodeTaskDelegate;

/**
 * Created by vadimbalasov on 24.03.16.
 */
public class RadarsViewModel implements DownloadSettingsTaskDelegate, SendPromoCodeTaskDelegate {
    static RadarsViewModel _shared;

    public Radar[] radars;
    public Radar radar;

    public AccountManager accountManager;
    public RadarsViewModelDelegate delegate;

    public static RadarsViewModel shared() {
        if (_shared == null) {
            _shared = new RadarsViewModel();
        }
        return _shared;
    }

    public void loadSettings() {
        delegate.radarsStartLoading();

        Account[] list = accountManager.getAccounts();
        String accountHash = (list.length > 0) ? hash.sha1(list[0].name) : "unknown";

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        String sign = String.format("meteo%02d%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        String signHash = hash.sha1(sign);

        String url = String.format("http://meteo.bcr.by/auth.php?hash=%s&id=%s", accountHash, signHash);

        DownloadSettingsTask downloadSettingsTask = new DownloadSettingsTask(this);
        downloadSettingsTask.execute(url);
    }

    public void sendPromoCode(String promoCode) {
        delegate.promoStartSending();

        Account[] list = accountManager.getAccounts();
        String accountHash = (list.length > 0) ? hash.sha1(list[0].name) : "unknown";

        String encodedPromoCode;
        try {
            encodedPromoCode = URLEncoder.encode(promoCode, "utf-8");
        }
        catch (Exception e) {
            encodedPromoCode = promoCode;
        }
        String url = String.format("http://meteo.bcr.by/input.php?hash=%s&promo=%s", accountHash, encodedPromoCode);

        SendPromoCodeTask sendPromoCodeTask = new SendPromoCodeTask(this);
        sendPromoCodeTask.execute(url);
    }

    //region DownloadSettingsTaskDelegate

    public void onSettingsLoaded(Radar[] radars) {
        this.radars = radars;
        delegate.radarsLoaded(radars);
    }

    public void onSettingsNotLoaded() {
        delegate.radarsNotLoaded("");
    }

    //endregion

    //region SendPromoCodeTaskDelegate

    public void promoCodeSent() {
        delegate.promoSent();
    }

    public void promoCodeNotSent(String error) {
        delegate.promoFailedToSend(error);
    }

    //endregion
}
