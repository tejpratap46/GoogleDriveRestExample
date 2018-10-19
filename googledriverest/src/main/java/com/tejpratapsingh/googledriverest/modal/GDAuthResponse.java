package com.tejpratapsingh.googledriverest.modal;

import com.tejpratapsingh.googledriverest.Helper.GDException;

public class GDAuthResponse {

    public interface OnAuthResponseListener {
        void onSuccess(GDAuthResponse gdAuthResponse);

        void onError(GDException exception);
    }

    private String accessToken, refreshToken;
    private int expiresAtTimestamp;

    public GDAuthResponse(String accessToken, String refreshToken, int expiresInSec) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAtTimestamp = (int) (System.currentTimeMillis() / 1000) +  expiresInSec;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresAtTimestamp() {
        return expiresAtTimestamp;
    }

    public boolean isExpired() {
        if ((int) (System.currentTimeMillis() / 1000) > this.expiresAtTimestamp) {
            return true;
        }
        return false;
    }
}
