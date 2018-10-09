# GoogleDriveRestExample
Google Drive REST API for android

[![](https://jitpack.io/v/tejpratap46/GoogleDriveRestExample.svg)](https://jitpack.io/#tejpratap46/GoogleDriveRestExample)


Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.tejpratap46.GoogleDriveRestExample:googledriverest:1.1.1'
	}


# Example

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
                    showToast("Google Drive Authenticated");
                    File tempFile = GDFileManager.getInstance().createTempFile(getApplicationContext(), "txt",  false);
                    try {
                        GDFileManager.getInstance().saveStringToFile(tempFile, "This is a test file");

                        GDApiManager.getInstance().uploadFileAsync(gdAuthResponse, tempFile, GDFileManager.getInstance().getMimeType(getApplicationContext(), tempFile), true, new GDUploadFileResponse.OnUploadFileCompleteListener() {
                            @Override
                            public void onSuccess(GDUploadFileResponse uploadFileResponse) {

                                showToast("File Uploaded Successfully");
                                
                                // Download just uploaded file
                                GDApiManager.getInstance().downloadFileAsync(getApplicationContext(), gdAuthResponse, uploadFileResponse.getId(), "downloaded_file.txt", new GDDownloadFileResponse.OnDownloadFileCompleteListener() {
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
