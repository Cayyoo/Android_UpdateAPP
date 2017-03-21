package com.example.update.utils;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载调度管理器 调用UpdateDownloadRequest的方法执行下载操作
 * 可以通过两种方式：
 * 1.启动一个线程，去执行UpdateDownloadRequest这个runnable
 * 2.创建一个线程池，将UpdateDownloadRequest这个runnable扔进去执行
 */
public class UpdateManager {
    private static UpdateManager manager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    /**
     * 单例模式 构造函数是私有的
     */
    private UpdateManager(){
        threadPoolExecutor=(ThreadPoolExecutor)Executors.newCachedThreadPool();
    }

    /**
     * 单例模式
     */
    static {
        manager=new UpdateManager();
    }

    public static UpdateManager getInstance(){
        if (null == manager){
            manager=new UpdateManager();
        }
        return manager;
    }

    /**
     * 开始下载方法
     */
    public void startDownloads(String downloadUrl,String localPath,UpdateDownloadListener listener){
        if (request!=null){
            return;
        }

        checkLocalFilePath(localPath);

        //开始真正的去下载任务
        request=new UpdateDownloadRequest(downloadUrl,localPath,listener);
        Future<?> future=threadPoolExecutor.submit(request);
    }

    /**
     * 用来检查文件路劲是否已经存在
     */
    private void checkLocalFilePath(String path){
        Log.e("tag", path);

        File dir=new File(path.substring(0,path.lastIndexOf("/")+1));
        if (!dir.exists()){
            dir.mkdir();
        }

        File file=new File(path);
        if (!file.exists()){
            try {
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
