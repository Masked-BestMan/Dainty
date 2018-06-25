package com.zbm.dainty.bean;

import android.support.annotation.NonNull;

public class FileDownloadBean implements Comparable{
    private String downloadUrl;
    private boolean isDownloading,isFinished;
    private String fileName,filePath;
    private String speed="--KB/s";
    private long lastModified;
    private int downloadProgress,fileSize;

    public FileDownloadBean(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileDownloadBean)
            return fileName.equals(((FileDownloadBean)obj).getFileName());
        else
            return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return Long.compare(((FileDownloadBean) o).getLastModified(), getLastModified());
    }
}
