package com.tejpratapsingh.gogledriverest.modal;

import com.tejpratapsingh.gogledriverest.Helper.GDException;

public class GDUploadFileResponse {

    public interface OnUploadFileCompleteListener {
        void onSuccess(GDUploadFileResponse uploadFileResponse);
        void onError(GDException exception);
    }

    private String id, name, mimeType;

    public GDUploadFileResponse(String id, String name, String mimeType) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }
}
