package com.example.update;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.update.dialog.TwoStyleDialog;
import com.example.update.utils.UpdateService;
import com.example.update.dialog.OneStyleDialog;

/**
 * 应用自动更新组件开发：
 * 1、自定义更新提示对话框
 * 2、自动更新APP
 */
public class MainActivity extends AppCompatActivity {
    private Button button;

    //apk下载链接
    private static final String APK_DOWNLOAD_URL = "http://gdown.baidu.com/data/wisegame/f98d235e39e29031/baiduxinwen.apk";
    //private static final String APK_DOWNLOAD_URL = "http://app.jikexueyuan.com/GeekAcademy_release_jikexueyuan_aligned.apk";

    //权限请求参数
    private static final int REQUEST_WRITE_STORAGE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        button=(Button)this.findViewById(R.id.btn_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVersion();
            }
        });

        //checkVersion();
        //checkVersionDiglogOne();
        checkVersionDiglogTwo();
    }

    /**
     * 自定义对话框样式一
     */
    private void checkVersionDiglogOne() {
        final OneStyleDialog dialog = new OneStyleDialog(this);

        dialog.setTitle("ApkUpdate");
        dialog.setContent("发现新版本，请及时更新");
        dialog.setLeftBtnText("立即更新");
        dialog.setRightBtnText("稍后再说");

        dialog.setOnYesClickListener(new OneStyleDialog.OnYesClickListener() {
            @Override
            public void yesClick() {
                dialog.dismiss();

                requestPermission();
            }
        });

        dialog.setOnNoClickListener(new OneStyleDialog.OnNoClickListener() {
            @Override
            public void noClick() {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 自定义对话框样式二
     */
    private void checkVersionDiglogTwo(){//这里不发送检测新版本网络请求，直接进入下载新版本安装
        TwoStyleDialog.Builder builder = new TwoStyleDialog.Builder(this);

        builder.setTitle("升级提示");
        builder.setMessage("发现新版本，请及时更新");

        builder.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                requestPermission();
            }
        });

        builder.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * 系统弹窗对话框样式
     */
    private void checkVersion(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("您有应用更新")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        requestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    /**
     * 启动下载，开启更新
     */
    private void startDownload() {
        Intent intent = new Intent(MainActivity.this, UpdateService.class);
        //注意这里传入的两个参数，与UpdateService接收的参数必须一致
        intent.putExtra("apkUrl", APK_DOWNLOAD_URL);
        startService(intent);
    }


    /**
     * 权限请求
     */
    private void requestPermission() {
        //请求存储权限
        boolean hasPermission = (
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
        );

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE
            );

            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            //下载
            startDownload();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取到存储权限,进行下载
                    startDownload();
                } else {
                    Toast.makeText(MainActivity.this, "不授予存储权限将无法进行下载!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
