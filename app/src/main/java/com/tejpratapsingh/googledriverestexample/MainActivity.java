package com.tejpratapsingh.googledriverestexample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

import com.tejpratapsingh.googledriverest.Helper.GDException;
import com.tejpratapsingh.googledriverest.Helper.GDFileManager;
import com.tejpratapsingh.googledriverest.api.GDApiManager;
import com.tejpratapsingh.googledriverest.auth.GDAuthConfig;
import com.tejpratapsingh.googledriverest.auth.GDAuthManager;
import com.tejpratapsingh.googledriverest.modal.GDAuthResponse;
import com.tejpratapsingh.googledriverest.modal.GDDownloadFileResponse;
import com.tejpratapsingh.googledriverest.modal.GDUploadFileResponse;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GDAuthConfig gdAuthConfig;

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
            this.gdAuthConfig = new GDAuthConfig("https://httpbin1.appspot.com/get",
                    "CLIENT_ID",
                    "CLIENT_SECRET",
                    scopes);

            final ProgressDialog loadingDialog = new ProgressDialog(this); // this = YourActivity
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.setTitle("Loading");
            loadingDialog.setMessage("Loading. Please wait...");
            loadingDialog.setIndeterminate(true);
            loadingDialog.setCanceledOnTouchOutside(false);

            gdAuthManager.startGoogleDriveAuth(MainActivity.this, webViewGoogleDrive, this.gdAuthConfig, new GDAuthManager.OnGoogleAuthCompleteListener() {
                @Override
                public void onLoadingStart() {
                    // Show loading alert
                    loadingDialog.show();
                }

                @Override
                public void onLoadingFinish() {
                    loadingDialog.dismiss();
                }

                @Override
                public void onSuccess(final GDAuthResponse gdAuthResponse) {
                    // Upload a file
                    showToast("Google Drive Authenticated");
                    File tempFile = GDFileManager.getInstance().createTempFile(getApplicationContext(), "txt",  false);
                    try {
                        GDFileManager.getInstance().saveStringToFile(tempFile, "This is a test file");

                        GDApiManager.getInstance().uploadFileAsync(getApplicationContext(), gdAuthResponse, MainActivity.this.gdAuthConfig, tempFile, GDFileManager.getInstance().getMimeType(getApplicationContext(), tempFile), true, new GDUploadFileResponse.OnUploadFileCompleteListener() {
                            @Override
                            public void onSuccess(GDUploadFileResponse uploadFileResponse) {

                                showToast("File Uploaded Successfully");
                                
                                // Download just uploaded file
                                GDApiManager.getInstance().downloadFileAsync(getApplicationContext(), gdAuthResponse, MainActivity.this.gdAuthConfig, uploadFileResponse.getId(), "downloaded_file.txt", new GDDownloadFileResponse.OnDownloadFileCompleteListener() {
                                    @Override
                                    public void onSuccess(File downloadedFile) {
                                        // Check for a download file in your private files
                                        // In here: Internal Storage > Android > data > com.tejpratapsingh.com > files
                                        showToast("File Downloaded Successfully");
                                    }

                                    @Override
                                    public void onError(GDException exception) {
                                        showToast("Error: " + exception.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onError(GDException exception) {
                                showToast("Error: " + exception.getMessage());
                            }
                        });
                    } catch (GDException e) {
                        e.printStackTrace();
                        showToast("Error: " + e.getMessage());
                    }
                }

                @Override
                public void onError(GDException exception) {
                    exception.printStackTrace();
                    showToast("Error: " + exception.getMessage());
                }
            });
        } catch (GDException e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
