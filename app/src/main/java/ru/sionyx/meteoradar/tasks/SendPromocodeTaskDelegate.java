package ru.sionyx.meteoradar.tasks;

/**
 * Created by vadimbalasov on 29.03.16.
 */
public interface SendPromoCodeTaskDelegate {
    void promoCodeSent();
    void promoCodeNotSent(String error);
}
