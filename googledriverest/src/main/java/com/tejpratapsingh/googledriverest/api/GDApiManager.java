package com.tejpratapsingh.googledriverest.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tejpratapsingh.googledriverest.Helper.GDException;
import com.tejpratapsingh.googledriverest.Helper.GDFileManager;
import com.tejpratapsingh.googledriverest.auth.GDAuthConfig;
import com.tejpratapsingh.googledriverest.modal.GDAuthResponse;
import com.tejpratapsingh.googledriverest.modal.GDDownloadFileResponse;
import com.tejpratapsingh.googledriverest.modal.GDUploadFileResponse;
import com.tejpratapsingh.googledriverest.modal.GDUserInfo;

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

    public void getGetAuthCodeAsync(final String code, final GDAuthConfig config, final GDAuthResponse.OnAuthResponseListener onAuthResponseListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    onAuthResponseListener.onSuccess(getInstance().getGetAuthCode(code, config));
                } catch (GDException e) {
                    onAuthResponseListener.onError(e);
                }
            }
        });
    }

    public GDAuthResponse getGetAuthCode(final String code, final GDAuthConfig config) throws GDException {
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

                return new GDAuthResponse(responseJSON.getString("access_token"), responseJSON.getString("refresh_token"));

            } catch (JSONException e) {
                e.printStackTrace();
                throw new GDException(e.getMessage());
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new GDException(e.getMessage());
        }
    }

    /**
     * Get user information if you asked for SCOPE: EMAIL
     * in BACKGROUND
     *
     * @param gdAuthResponse             Auth credentials
     * @param onUserInfoReceivedListener onComplete event listener
     */
    public void getUserInfoAsync(final GDAuthResponse gdAuthResponse, final GDUserInfo.OnUserInfoReceivedListener onUserInfoReceivedListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    onUserInfoReceivedListener.onSuccess(getInstance().getUserInfo(gdAuthResponse));
                } catch (GDException e) {
                    e.printStackTrace();
                    onUserInfoReceivedListener.onError(e);
                }
            }
        });
    }

    /**
     * Get user information if you asked for SCOPE: EMAIL
     * in CURRENT thread
     *
     * @param gdAuthResponse Auth credentials
     * @return user info
     * @throws GDException if any error occurred
     */
    public GDUserInfo getUserInfo(final GDAuthResponse gdAuthResponse) throws GDException {
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + gdAuthResponse.getAccessToken())
                .get()
                .build();

        try {
            Response response = getInstance().client.newCall(request).execute();

            try {
                JSONObject userJSONObject = new JSONObject(response.body().string());

                return new GDUserInfo(userJSONObject.getString("name"),
                        userJSONObject.getString("email"),
                        userJSONObject.getString("profile"),
                        userJSONObject.getString("picture"));

            } catch (JSONException e) {
                e.printStackTrace();
                throw new GDException(e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new GDException(e.getMessage());
        }
    }

    /**
     * Upload file to google drive in BACKGROUND thread
     *
     * @param gdAuthResponse     auth credentials from google response
     * @param fileToUpload       file to upload to google drive
     * @param fileMime           mime type of file, can be fetched from GDFileManager.getMimeType
     * @param uploadToAppFolder  true if you want to use app folder in google drive (files won't be visible to user)
     * @param uploadFileListener listener for success or exception
     */
    public void uploadFileAsync(final GDAuthResponse gdAuthResponse, final File fileToUpload, final String fileMime, final boolean uploadToAppFolder, final GDUploadFileResponse.OnUploadFileCompleteListener uploadFileListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    GDUploadFileResponse gdUploadFileResponse = getInstance().uploadFile(gdAuthResponse, fileToUpload, fileMime, uploadToAppFolder);
                    uploadFileListener.onSuccess(gdUploadFileResponse);
                } catch (GDException e) {
                    uploadFileListener.onError(e);
                }
            }
        });
    }

    /**
     * Upload file to google drive in CURRENT thread
     *
     * @param gdAuthResponse    auth credentials from google response
     * @param fileToUpload      file to upload to google drive
     * @param fileMime          mime type of file, can be fetched from GDFileManager.getMimeType
     * @param uploadToAppFolder true if you want to use app folder in google drive (files won't be visible to user)
     * @return GDUploadFileResponse object with fileId and name
     * @throws GDException if any error occurred
     */
    public GDUploadFileResponse uploadFile(final GDAuthResponse gdAuthResponse, final File fileToUpload, final String fileMime, final boolean uploadToAppFolder) throws GDException {
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

                return new GDUploadFileResponse(
                        fileUploadResponseJSONObject.getString("id"),
                        fileUploadResponseJSONObject.getString("name"),
                        fileUploadResponseJSONObject.getString("mimeType")
                );
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw new GDException(e.getMessage());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new GDException(e.getMessage());
        }
    }


    /**
     * Download file in BACKGROUND thread
     *
     * @param context                      to get local folder of application
     * @param gdAuthResponse               Auth credentials
     * @param gdFileId                     fileId to download
     * @param fileName                     name of saved file
     * @param downloadFileCompleteListener on complete event
     */
    public void downloadFileAsync(final Context context, final GDAuthResponse gdAuthResponse, final String gdFileId, final String fileName, final GDDownloadFileResponse.OnDownloadFileCompleteListener downloadFileCompleteListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    downloadFileCompleteListener.onSuccess(getInstance().downloadFile(context, gdAuthResponse, gdFileId, fileName));
                } catch (GDException e) {
                    downloadFileCompleteListener.onError(e);
                }
            }
        });
    }


    /**
     * Download file in CURRENT thread
     *
     * @param context        to get local folder of application
     * @param gdAuthResponse Auth credentials
     * @param gdFileId       fileId to download
     * @param fileName       name of saved file
     * @return saved File
     * @throws GDException if any error occurred
     */
    public File downloadFile(final Context context, final GDAuthResponse gdAuthResponse, final String gdFileId, final String fileName) throws GDException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files/" + gdFileId + "?alt=media")
                .get()
                .addHeader("authorization", "Bearer " + gdAuthResponse.getAccessToken())
                .build();

        try {
            Response response = client.newCall(request).execute();
            InputStream fileInputStream = response.body().byteStream();

            File savedFile = GDFileManager.getInstance().saveFileToPrivateStorageFromInputStream(context, fileInputStream, fileName, true);

            return savedFile;
        } catch (IOException | GDException e) {
            e.printStackTrace();
            throw new GDException(e.getMessage());
        }

    }
}
