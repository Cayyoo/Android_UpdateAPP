package com.example.update.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.update.R;

import java.io.File;

/**
 * app更新下载后台服务
 * 启动service调用UpdateManager的方法进行下载
 */
public class UpdateService extends Service {
    private String apkURL;
    private String filePath;
    private NotificationManager notificationManager;
    private Notification notification;

    @Override
    public void onCreate() {
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        filePath= Environment.getExternalStorageDirectory()+"/UpdateAPP/ApkUpdate.apk"; //要存的文件路径
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent==null){
            notifyUser(getString(R.string.update_download_failed),getString(R.string.update_download_failed_msg),0);
            stopSelf();
        }else {
            apkURL=intent.getStringExtra("apkUrl");
            //apkURL=intent.getStringExtra("http://gdown.baidu.com/data/wisegame/f98d235e39e29031/baiduxinwen.apk");
            Log.i("Tiger","下载地址："+apkURL);

            notifyUser(getString(R.string.update_download_start), getString(R.string.update_download_start),0);
            startDownload();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 开始下载
     */
    private void startDownload(){
        UpdateManager.getInstance().startDownloads(apkURL, filePath, updateDownloadListener);
    }

    private UpdateDownloadListener updateDownloadListener=new UpdateDownloadListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onProgressChanged(int progress, String downloadUrl) {
            notifyUser(getString(R.string.update_download_processing),getString(R.string.update_download_processing),progress);
        }

        @Override
        public void onFinished(int completeSize, String downloadUrl) {
            notifyUser(getString(R.string.update_download_finish),getString(R.string.update_download_finish),100);
            stopSelf();
        }
        @Override
        public void onFailure() {
            notifyUser(getString(R.string.update_download_failed),getString(R.string.update_download_failed_msg),0);
            stopSelf();
        }
    };

    /**
     * 更新notification来告知用户当前下载的进度
     */
    private void notifyUser(String result,String reason,int progress){
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name));

        if (progress>0 && progress<100){
            builder.setProgress(100,progress,false);//显示
        }else {
            builder.setProgress(0,0,false);//隐藏
        }

        builder.setAutoCancel(true);//可以被我们自动清除掉
        builder.setWhen(System.currentTimeMillis());//系统当前时间
        builder.setTicker(result);
        builder.setContentIntent(
                progress >= 100 ? getContentIntent() :
                        PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT)
        );

        notification=builder.build();
        notificationManager.notify(0, notification);

    }

    /**
     * 进入apk安装程序
     */
    private PendingIntent getContentIntent(){
        File apkFile=new File(filePath);
        Intent intent=new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        startActivity(intent);

        return pendingIntent;
    }

}
