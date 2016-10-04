package com.example.masterkdk.methodverification.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

//import com.intc_service.tejun.confirmatointablet.net.TcpClient;
import com.example.masterkdk.methodverification.net.TcpClient;

/**
 * サーバーへ要求を送信するためのLoader
 */

public class SendRequestLoader extends AsyncTaskLoader<String> {
    private String mSendData;
    private String mHost;
    private int mPort;
    // 結果
    private byte[] mData;
    private TcpClient client;
    public SendRequestLoader(Context context, String host, int port, String senddata ){
        super(context);
        mHost = host;
        mPort = port;
        mSendData = senddata;
    }

    @Override
    public String loadInBackground() {
        client = new TcpClient(mHost, mPort, mSendData);

        String message = client.connect();
        return message;

//        return client.connect();
    }

       @Override
    protected void onStartLoading() {
        // ロード開始処理 結果のキャッシュなど判定する
        forceLoad();  // lodInBackgroudが実行される
    }
    @Override
    protected void onStopLoading() {
        // 停止処理
    }
    @Override
    protected void onReset() {
        // リセット処理
    }
}