package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.alertDialogUtil;

/**
 * Created by masterkdk on 2016/09/26.
 * S-06 終了方法選択画面
 */

public class EndActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";

    private TransmissionFragment sendFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // ボタンへリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
        findViewById(R.id.all_end_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        Intent intent = null;
        if (id == R.id.return_button) {
            intent = new Intent(this, TopActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);

        } else if (id == R.id.all_end_button) {
            // 全てのタブレットを終了する

            // Fragmentを利用した通信の準備
            sendFragment = TransmissionFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(sendFragment, TAG_TRANS);
            transaction.commit();
            fragmentManager.executePendingTransactions();   // 即時実行

            // 終了コマンド送信
            DataStructureUtil dataStructureHelper = new DataStructureUtil();
            String data = dataStructureHelper.makeSendData("16","");
            sendFragment.send(data);
        }
    }

    // サーバからの応答受信
    @Override
    public void onResponseRecieved(String data) {

        System.out.println("ResRecieved");

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        System.out.println("Command：" + cmd);

        // 応答が正常終了だったら終了画面へ遷移
        if (cmd.equals("50")) {
            Intent intent = new Intent(this, EndOffActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);

        } else if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 実装する処理はないが、インターフェイス利用の為にオーバーライドが必要
    }
}
