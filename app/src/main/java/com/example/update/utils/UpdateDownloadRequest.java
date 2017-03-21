package com.example.update.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 负责处理文件的下载和线程间的通信
 * 真正执行下载的runnable
 */
public class UpdateDownloadRequest implements  Runnable {

    private String downloadUrl;//文件下载路径
    private String localFilePath;//文件保存路径
    private UpdateDownloadListener downloadListener;//接口回调
    private boolean isDownloading=false;//下载的标志位
    private long currentLength;//文件长度

    private DownloadResponseHandler downloadHanlder;

    public UpdateDownloadRequest(String downloadUrl,String localFilePath,UpdateDownloadListener downloadListener) {
        this.downloadListener=downloadListener;
        this.downloadUrl=downloadUrl;
        this.localFilePath=localFilePath;
        this.isDownloading=true;
        this.downloadHanlder=new DownloadResponseHandler();
    }

    /**
     * 真正的去建立连接的方法
     */
    private void makeRequest() throws IOException,InterruptedException{
        if (!Thread.currentThread().isInterrupted()){
            try {
                URL url=new URL(downloadUrl);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();//阻塞当前的线程
                currentLength=connection.getContentLength();

                if (!Thread.currentThread().isInterrupted()){
                    //真正的完成文件的下载
                    downloadHanlder.sendResponseMessage(connection.getInputStream());
                }
            }catch(IOException e){
                throw e;
            }
        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化浮点数 只保留两位小数
     *
     * @param value 传入的string类型的value
     * @return
     */
    private String getTwoPointFloatStr(float value){
        DecimalFormat fnum=new DecimalFormat("0.00");
        return fnum.format(value);
    }

    /**
     * 包含了下载过程中所有可能出现的异常情况
     */
    public enum FailureCode{
        UnknownHost, Socket, SocketTimeout, ConnectTimeout, IO, HttpResponse, JSON, Interrupted
    }

    /**
     * 用来真正的去下载文件，并发送息和回调的接口
     */
    public class DownloadResponseHandler{
        protected static final int SUCCESS_MESSAGE=0;
        protected static final int FAILURE_MESSAGE=1;
        protected static final int START_MESSAGE=2;
        protected static final int FINISH_MESSAGE=3;
        protected static final int NETWORK_OFF=4;
        private static final int PROGRESS_CHANGED=5;

        private int mCompleteSize=0;
        private int progress=0;

        private Handler handler;//真正的完成线程间的通信

        public DownloadResponseHandler(){
            handler=new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };
        }

        /**
         * 用来发送不同的消息对象
         */
        protected void sendFinishMessage(){
            sendMessage(obtainMessage(FINISH_MESSAGE,null));
        }

        private void sendProgressChangedMessage(int progress){
            sendMessage(obtainMessage(PROGRESS_CHANGED,new Object[]{progress}));
        }

        protected void sendFailureMessage(FailureCode failureCode){
            sendMessage(obtainMessage(FAILURE_MESSAGE,new Object[]{failureCode}));
        }

        protected void sendMessage(Message msg){
            if (handler !=null){
                handler.sendMessage(msg);
            }else {
                handleSelfMessage(msg);
            }
        }

        /**
         * 获取一个消息对象
         */
        protected Message obtainMessage(int responseMessage,Object response){
            Message msg=null;

            if (handler!=null){
                msg=handler.obtainMessage(responseMessage,response);
            }else{
                msg=Message.obtain();
                msg.what=responseMessage;
                msg.obj=response;
            }

            return msg;
        }


        protected void handleSelfMessage(Message msg){
            Object[] response;

            switch (msg.what){
                case FAILURE_MESSAGE:
                    response= (Object[]) msg.obj;
                    handleFailureMessage((FailureCode)response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response= (Object[]) msg.obj;
                    handleProgressChangedMessage((Integer) response[0]);
                    //handleProgressChangedMessage(((Integer)response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }

        }

        /**
         * 各种消息的处理逻辑
         */
        protected void handleProgressChangedMessage(int progress){
            //downloadListener.onProgressChanged(progress,"");
            downloadListener.onProgressChanged(progress,downloadUrl);
        }

        protected void handleFailureMessage(FailureCode failureCode){
            onFailure(failureCode);
        }

        //外部接口的回调
        public void onFinish(){
            downloadListener.onFinished(mCompleteSize,"");
        }

        public void onFailure(FailureCode failureCode){
           downloadListener.onFailure();
        }

        /**
         * 文件下载方法，会发送各种类型的事件
         */
        void sendResponseMessage(InputStream is){
            RandomAccessFile randomAccessFile=null;//文件读写流
            mCompleteSize=0;

            try{
                byte[] buffer=new byte[1024];
                int length=-1;//读写长度
                int limit=0;
                randomAccessFile=new RandomAccessFile(localFilePath,"rwd");//可读可写模式

                while ((length=is.read(buffer))!=-1){
                    if (isDownloading){
                        randomAccessFile.write(buffer,0,length);
                        mCompleteSize+=length;

                        if (mCompleteSize<currentLength){
                            //progress= (int) Float.parseFloat(getTwoPointFloatStr(mCompleteSize/currentLength));
                            progress = (int)(Float.parseFloat(getTwoPointFloatStr(mCompleteSize/currentLength))*100);

                            if (limit/30 ==0 || progress<=100){
                                //为了限制一下notification的更新频率
                                sendProgressChangedMessage(progress);
                            }

                            /*if (progress >= 100) {
                                //下载完成
                                sendProgressChangedMessage(progress);
                            }*/

                            limit++;
                        }
                    }
                }
                sendFinishMessage();
            }catch (IOException e){
                sendFailureMessage(FailureCode.IO);
            }finally {
                try{
                    if (is!=null){
                        is.close();
                    }

                    if (randomAccessFile!=null){
                        randomAccessFile.close();
                    }
                }catch (IOException e){
                    sendFailureMessage(FailureCode.IO);
                }

            }
        }

    }

}
