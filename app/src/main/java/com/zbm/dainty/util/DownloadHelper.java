package com.zbm.dainty.util;

import com.zbm.dainty.bean.FileDownloadBean;
import com.zbm.dainty.task.DownloaderTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DownloadHelper {
    /**
     * 下载队列
     */
    public final static int downloadLimitCount=1;   //当前正在下载的文件数
    public static List<DownloaderTask> downloadList=new LinkedList<>();   //正在下载任务列表

    public static void stopAllDownloads(){
        for (DownloaderTask downloaderTask:downloadList){
            downloaderTask.cancel(true);
        }
    }

    public static DownloaderTask getDownloadFile(String filePath){
        for (DownloaderTask task:downloadList){
            if (task.getFilePath().equals(filePath))
                return task;
        }
        return null;
    }
}
