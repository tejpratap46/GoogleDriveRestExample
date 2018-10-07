package com.tejpratapsingh.gogledriverest.modal;

import com.tejpratapsingh.gogledriverest.Helper.GDException;

public class GDAuthResponse {

    public interface OnAuthResponseListener {
        void onSuccess(GDAuthResponse gdAuthResponse);

        void onError(GDException exception);
    }

    private String accessToken, refreshToken;

    public GDAuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
