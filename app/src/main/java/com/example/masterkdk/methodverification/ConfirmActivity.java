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
 * Created by masterkdk on 2016/09/23.
 * S-03 確認画面
 */

public class ConfirmActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // ボタンへリスナを登録
        findViewById(R.id.ok_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.ok_button) {

            // Fragmentを利用した通信の準備
            TransmissionFragment sendFragment = TransmissionFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(sendFragment, TAG_TRANS);
            transaction.commit();
            fragmentManager.executePendingTransactions();   // 即時実行

            // OKコマンド送信
            DataStructureUtil dataStructureHelper = new DataStructureUtil();
            String data = dataStructureHelper.makeSendData("17","");
            sendFragment.send(data);
/*
            // デバッグ用の仮遷移処理
            Intent intent = new Intent(this, ProcedureActivity.class);
            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));
            startActivity(intent);
*/
        }
    }

    // サーバからの応答受信
    @Override
    public void onResponseRecieved(String data) {

        System.out.println("ResRecieved");

        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        System.out.println("Command：" + cmd);

        // 応答が正常終了だったら手順書画面へ遷移
        if (cmd.equals("50")) {
            Intent intent = new Intent(this, ProcedureActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);

        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※" + data);
            alertDialogUtil.show(this, getResources().getString(R.string.nw_err_title), getResources().getString(R.string.nw_err_message));
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // インターフェイス利用に必要なオーバーライド
    }
}
