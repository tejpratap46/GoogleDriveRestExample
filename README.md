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
	        implementation 'com.github.tejpratap46.GoogleDriveRestExample:gogledriverest:1.0'
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
                    File tempFile = GDFileManager.getInstance().createTempFile(getApplicationContext(), "txt",  false);
                    try {
                        GDFileManager.getInstance().saveStringToFile(tempFile, "This is a test file");

                        GDApiManager.getInstance().uploadFile(gdAuthResponse, tempFile, GDFileManager.getInstance().getMimeType(getApplicationContext(), tempFile), false, new GDUploadFileResponse.OnUploadFileCompleteListener() {
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
