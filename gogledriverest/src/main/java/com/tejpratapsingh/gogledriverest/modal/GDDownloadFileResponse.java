package com.tejpratapsingh.gogledriverest.modal;

import com.tejpratapsingh.gogledriverest.Helper.GDException;

import java.io.File;

public class GDDownloadFileResponse {
    public interface OnDownloadFileCompleteListener {
        void onSuccess(File downloadedFile);
        void onError(GDException exception);
    }
}
