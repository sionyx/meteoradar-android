package ru.sionyx.meteoradar.ViewModels;

import ru.sionyx.meteoradar.Radar;

/**
 * Created by vadimbalasov on 24.03.16.
 */
public interface RadarsViewModelDelegate {

    void radarsStartLoading();
    void radarsLoaded(Radar[]radars);
    void radarsNotLoaded(String error);
    void promoStartSending();
    void promoSent();
    void promoFailedToSend(String error);

}
