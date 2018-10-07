package com.tejpratapsingh.googledriverestexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.tejpratapsingh.gogledriverest.Helper.GDException;
import com.tejpratapsingh.gogledriverest.Helper.GDFileManager;
import com.tejpratapsingh.gogledriverest.api.GDApiManager;
import com.tejpratapsingh.gogledriverest.auth.GDAuthConfig;
import com.tejpratapsingh.gogledriverest.auth.GDAuthManager;
import com.tejpratapsingh.gogledriverest.modal.GDAuthResponse;
import com.tejpratapsingh.gogledriverest.modal.GDDownloadFileResponse;
import com.tejpratapsingh.gogledriverest.modal.GDUploadFileResponse;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webViewGoogleDrive = (WebView) findViewById(R.id.webViewGoogleDrive);

        GDAuthManager gdAuthManager = GDAuthManager.getInstance();

        try {

            ArrayList<GDAuthConfig.SCOPES> scopes = new ArrayList<>();
            scopes.add(GDAuthConfig.SCOPES.EMAIL);
            scopes.add(GDAuthConfig.SCOPES.APP_FOLDER);
            GDAuthConfig gdAuthConfig = new GDAuthConfig("https://httpbin1.appspot.com/get",
                    "CLIENT_ID",
                    "CLIENT_SECRET",
                    scopes);

            gdAuthManager.startGoogleDriveAuth(webViewGoogleDrive, gdAuthConfig, new GDAuthManager.OnGoogleAuthCompleteListener() {
                @Override
                public void onSuccess(final GDAuthResponse gdAuthResponse) {
                    // Upload a file
                    File tempFile = GDFileManager.getInstance().createTempFile(getApplicationContext(), ".txt",  false);
                    try {
                        GDFileManager.getInstance().saveStringToFile(tempFile, "This is a test file");

                        GDApiManager.getInstance().uploadFile(gdAuthResponse, tempFile, GDFileManager.getInstance().getMimeType(getApplicationContext(), tempFile), new GDUploadFileResponse.OnUploadFileCompleteListener() {
                            @Override
                            public void onSuccess(GDUploadFileResponse uploadFileResponse) {
                                // Download just uploaded file
                                GDApiManager.getInstance().downloadFile(getApplicationContext(), gdAuthResponse, uploadFileResponse.getId(), "downloaded_file.txt", new GDDownloadFileResponse.OnDownloadFileCompleteListener() {
                                    @Override
                                    public void onSuccess(File downloadedFile) {
                                        // Check for a download file in your private files
                                        // In here: Internal Storage > Android > data > com.tejpratapsingh.com > files
                                    }

                                    @Override
                                    public void onError(GDException exception) {
                                        Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(GDException exception) {
                                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (GDException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(GDException exception) {
                    exception.printStackTrace();
                    Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (GDException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
