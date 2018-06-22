package com.zbm.dainty.bean;

import android.support.annotation.NonNull;

public class FileDownloadBean implements Comparable{
    private boolean isDownloading;
    private String fileName,fileSize,fileSuffix,filePath;
    private long lastModified;

    public FileDownloadBean(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return Long.compare(((FileDownloadBean) o).getLastModified(), getLastModified());
    }
}
