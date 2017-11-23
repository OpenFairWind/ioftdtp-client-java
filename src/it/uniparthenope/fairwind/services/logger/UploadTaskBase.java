package it.uniparthenope.fairwind.services.logger;

import java.io.File;
import java.util.ArrayList;

public abstract class UploadTaskBase {
    private String httpClientClassName;
    private String uploadUrl;
    private Uploader uploader;


    public UploadTaskBase(String httpClientClassName, String uploadUrl, Uploader uploader) {
        this.httpClientClassName=httpClientClassName;
        this.uploadUrl=uploadUrl;
        this.uploader=uploader;
    }

    public String getHttpClientClassName() { return httpClientClassName; }

    public String getUploadUrl() { return uploadUrl; }

    public Uploader getUploader() { return uploader; }

    public abstract void execute(File[] files);

    public boolean isUploading(String filePath) {
        return uploader.isUploading(filePath);
    }

    public void putInUploading(String filePath) {
        uploader.putInUploading(filePath);
    }

    public int getAvailableClients() {
        return uploader.getAvailableClients();
    }

    public int getCurrentClients() {
        return uploader.getCurrentClients();
    }
}
