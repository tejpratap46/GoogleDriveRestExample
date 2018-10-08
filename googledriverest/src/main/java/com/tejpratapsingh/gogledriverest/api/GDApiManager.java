package com.tejpratapsingh.gogledriverest.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tejpratapsingh.gogledriverest.Helper.GDException;
import com.tejpratapsingh.gogledriverest.Helper.GDFileManager;
import com.tejpratapsingh.gogledriverest.auth.GDAuthConfig;
import com.tejpratapsingh.gogledriverest.modal.GDAuthResponse;
import com.tejpratapsingh.gogledriverest.modal.GDDownloadFileResponse;
import com.tejpratapsingh.gogledriverest.modal.GDUploadFileResponse;
import com.tejpratapsingh.gogledriverest.modal.GDUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GDApiManager {
    private static final String TAG = "GDApiManager";

    private OkHttpClient client;

    private static final GDApiManager ourInstance = new GDApiManager();

    public static GDApiManager getInstance() {
        return ourInstance;
    }

    private GDApiManager() {
        client = new OkHttpClient();
    }

    public void getGetAuthCode(final String code, final GDAuthConfig config, final GDAuthResponse.OnAuthResponseListener onAuthResponseListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                String requestBody = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code", code, config.getClientId(), config.getClientSecret(), config.getRedirectURI());

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, requestBody);
                Request request = new Request.Builder()
                        .url("https://www.googleapis.com/oauth2/v4/token")
                        .post(body)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build();

                try {
                    Response response = getInstance().client.newCall(request).execute();
                    try {
                        JSONObject responseJSON = new JSONObject(response.body().string());

                        GDAuthResponse gdAuthResponse = new GDAuthResponse(responseJSON.getString("access_token"), responseJSON.getString("refresh_token"));

                        onAuthResponseListener.onSuccess(gdAuthResponse);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    onAuthResponseListener.onError(new GDException(e.getMessage()));
                }
            }
        });
    }

    public void getUserInfo(final GDAuthResponse gdAuthResponse, final GDUserInfo.OnUserInfoReceivedListener onUserInfoReceivedListener) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Request request = new Request.Builder()
                        .url("https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + gdAuthResponse.getAccessToken())
                        .get()
                        .build();

                try {
                    Response response = getInstance().client.newCall(request).execute();

                    try {
                        JSONObject userJSONObject = new JSONObject(response.body().string());

                        GDUserInfo userInfo = new GDUserInfo(userJSONObject.getString("name"),
                                userJSONObject.getString("email"),
                                userJSONObject.getString("profile"),
                                userJSONObject.getString("picture"));

                        onUserInfoReceivedListener.onSuccess(userInfo);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        onUserInfoReceivedListener.onError(new GDException(e.getMessage()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    onUserInfoReceivedListener.onError(new GDException(e.getMessage()));
                }
            }
        });
    }

    public void uploadFile(final GDAuthResponse gdAuthResponse, final File fileToUpload, final String fileMime, final boolean uploadToAppFolder, final GDUploadFileResponse.OnUploadFileCompleteListener uploadFileListener) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"name\": \"" + fileToUpload.getName() + "\"}");
                if (uploadToAppFolder) {
                    body = RequestBody.create(mediaType, "{\"name\": \"" + fileToUpload.getName() + "\", \"parents\":[\"appDataFolder\"]}");
                }
                Request fileCreateRequest = new Request.Builder()
                        .url("https://www.googleapis.com/drive/v3/files")
                        .post(body)
                        .addHeader("Authorization", "Bearer " + gdAuthResponse.getAccessToken())
                        .addHeader("accept", "application/json")
                        .addHeader("content-type", "application/json")
                        .build();

                try {
                    Response fileCreateResponse = getInstance().client.newCall(fileCreateRequest).execute();

                    JSONObject fileCreteResponseJSONObject = new JSONObject(fileCreateResponse.body().string());

                    MediaType mediaMimeType = MediaType.parse(fileMime);

                    Request fileUploadRequest = new Request.Builder()
                            .url("https://www.googleapis.com/upload/drive/v3/files/" + fileCreteResponseJSONObject.getString("id") + "?uploadType=media")
                            .patch(RequestBody.create(mediaMimeType, fileToUpload))
                            .addHeader("Authorization", "Bearer " + gdAuthResponse.getAccessToken())
                            .addHeader("Content-Type", fileMime)
                            .addHeader("Content-Length", "" + fileToUpload.length())
                            .build();

                    try {
                        Response fileUploadResponse = getInstance().client.newCall(fileUploadRequest).execute();

                        JSONObject fileUploadResponseJSONObject = new JSONObject(fileUploadResponse.body().string());

                        uploadFileListener.onSuccess(new GDUploadFileResponse(
                                fileUploadResponseJSONObject.getString("id"),
                                fileUploadResponseJSONObject.getString("name"),
                                fileUploadResponseJSONObject.getString("mimeType"))
                        );
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        uploadFileListener.onError(new GDException(e.getMessage()));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    uploadFileListener.onError(new GDException(e.getMessage()));
                }
            }
        });
    }

    public void downloadFile(final Context context, final GDAuthResponse gdAuthResponse, final String GDFileId, final String fileName, final GDDownloadFileResponse.OnDownloadFileCompleteListener downloadFileCompleteListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://www.googleapis.com/drive/v3/files/" + GDFileId + "?alt=media")
                        .get()
                        .addHeader("authorization", "Bearer " + gdAuthResponse.getAccessToken())
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    InputStream fileInputStream = response.body().byteStream();

                    File savedFile = GDFileManager.getInstance().saveFileToPrivateStorageFromInputStream(context, fileInputStream, fileName, true);

                    downloadFileCompleteListener.onSuccess(savedFile);

                    Log.d(TAG, "run: Download success");

                } catch (IOException | GDException e) {
                    e.printStackTrace();
                    downloadFileCompleteListener.onError(new GDException(e.getMessage()));
                }
            }
        });
    }
}
