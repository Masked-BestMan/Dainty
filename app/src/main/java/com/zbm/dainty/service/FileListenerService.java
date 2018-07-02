package com.zbm.dainty.service;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public class FileListenerService extends Service {
    private SDCardListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        listener=new SDCardListener("/storage/emulated/0/DaintyDownloads",this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener.startWatching();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.stopWatching();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class SDCardListener extends FileObserver{
        private WeakReference<FileListenerService> weakReference;

        SDCardListener(String path, FileListenerService service) {
            /*
             * 这种构造方法是默认监听所有事件的,如果使用 super(String,int)这种构造方法，
             * 则int参数是要监听的事件类型.
             */
            super(path);
            weakReference=new WeakReference<>(service);
        }

        @Override
        public void onEvent(int event, @Nullable String path) {
            switch (event){
                case MODIFY:
                case CREATE:
                case DELETE:
                    if (weakReference.get()!=null){
                    Intent intent = new Intent();
                    intent.setAction("download_progress_refresh");
                    intent.putExtra("finish_download", true);
                    weakReference.get().sendBroadcast(intent);
                }
            }


        }
    }

}
