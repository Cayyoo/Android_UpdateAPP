package com.example.update.utils;

/**
 * 事件的监听回调
 */
public interface UpdateDownloadListener {
    /**
     * 下载请求开始回调
     */
    public void onStarted();

    /**
     * 进度更新回调
     */
    public void onProgressChanged(int progress,String downloadUrl);

    /**
     * 下载完成回调
     */
    public void onFinished(int completeSize,String downloadUrl);

    /**
     * 下载失败回调
     */
    public void onFailure();


}
