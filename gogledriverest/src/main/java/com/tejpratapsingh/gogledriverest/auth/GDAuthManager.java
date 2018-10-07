package com.tejpratapsingh.gogledriverest.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tejpratapsingh.gogledriverest.Helper.GDConstants;
import com.tejpratapsingh.gogledriverest.Helper.GDException;
import com.tejpratapsingh.gogledriverest.Helper.GDUtilities;
import com.tejpratapsingh.gogledriverest.api.GDApiManager;
import com.tejpratapsingh.gogledriverest.modal.GDAuthResponse;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;

public class GDAuthManager {

    private static final String GD_PREFS_NAME = "GD_PREFS";

    public interface OnGoogleAuthCompleteListener {
        void onSuccess(GDAuthResponse gdAuthResponse);
        void onError(GDException exception);
    }

    private static final GDAuthManager ourInstance = new GDAuthManager();

    public static GDAuthManager getInstance() {
        return ourInstance;
    }

    private GDAuthManager() {
    }

    public void startGoogleDriveAuth(final WebView webView, final GDAuthConfig config, final OnGoogleAuthCompleteListener onGoogleAuthCompleteListener) {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebChromeClient(new MyWebChromeClient());
        enableHTML5AppCache(webView);

        webView.loadUrl(config.getAuthURL());

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                if (url.toLowerCase().startsWith(config.getRedirectURI())) {
                    // DO Auth API CALLS

                    try {
                        URL urlObject = new URL(url);
                        try {
                            String code = GDUtilities.splitQuery(urlObject).get("code");

                            GDApiManager gdApiManager = GDApiManager.getInstance();
                            gdApiManager.getGetAuthCode(code, config, new GDAuthResponse.OnAuthResponseListener() {
                                @Override
                                public void onSuccess(GDAuthResponse gdAuthResponse) {

                                    SharedPreferences.Editor editor = webView.getContext().getSharedPreferences(GDConstants.GD_PREFS_NAME, MODE_PRIVATE).edit();
                                    editor.putString(GDConstants.GD_PREFS_ACCESS_TOKEN, gdAuthResponse.getAccessToken());
                                    editor.putString(GDConstants.GD_PREFS_REFRESH_TOKEN, gdAuthResponse.getRefreshToken());
                                    editor.apply();

                                    onGoogleAuthCompleteListener.onSuccess(gdAuthResponse);
                                }

                                @Override
                                public void onError(GDException exception) {
                                    onGoogleAuthCompleteListener.onError(exception);
                                }
                            });
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            onGoogleAuthCompleteListener.onError(new GDException(e.getMessage()));
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        onGoogleAuthCompleteListener.onError(new GDException(e.getMessage()));
                    }

                }
            }
        });
    }

    public GDAuthResponse getAuthData(Context context) throws GDException {
        SharedPreferences preferences = context.getSharedPreferences(GDConstants.GD_PREFS_NAME, MODE_PRIVATE);

        if (preferences.contains(GDConstants.GD_PREFS_ACCESS_TOKEN) == false || preferences.contains(GDConstants.GD_PREFS_REFRESH_TOKEN) == false) {
            throw new GDException("Google is still not Authenticated");
        }

        return new GDAuthResponse(preferences.getString(GDConstants.GD_PREFS_ACCESS_TOKEN, null), preferences.getString(GDConstants.GD_PREFS_REFRESH_TOKEN, null));
    }

    private void enableHTML5AppCache(WebView mWebView) {

        mWebView.getSettings().setDomStorageEnabled(true);

        // Set cache size to 8 mb by default. should be more than enough
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        mWebView.getSettings().setAppCachePath("/data/data/" + mWebView.getContext().getPackageName() + "/cache");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
            WebView newWebView = new WebView(view.getContext());

            newWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            newWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
            newWebView.getSettings().setLoadWithOverviewMode(true);
            newWebView.getSettings().setAllowContentAccess(true);
            newWebView.getSettings().setDatabaseEnabled(true);
            newWebView.getSettings().setLoadsImagesAutomatically(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            enableHTML5AppCache(newWebView);
            newWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                }
            });
            view.addView(newWebView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
        }
    }
}
