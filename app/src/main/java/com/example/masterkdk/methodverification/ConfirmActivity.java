package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.example.masterkdk.methodverification.TransmissionFragment;
import com.example.masterkdk.methodverification.Helper.DataStructureHelper;
//import com.example.masterkdk.methodverification.loader.SendRequestLoader;
import com.example.masterkdk.methodverification.net.TcpClient;


/**
 * Created by masterkdk on 2016/09/23.
 * S-03 確認画面
 */

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {
//public class ConfirmActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener {

    private static final String HOST = "192.168.10.20";
//    private static final int PORT = 1280;  // ポート(実環境)
    private static final int PORT = 1234;  // ポート(VisualStudio)
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
            TransmissionFragment sendFragment = TransmissionFragment.newInstance(HOST,PORT);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(sendFragment, TAG_TRANS);
            transaction.commit();
            fragmentManager.executePendingTransactions();   // 即時実行

            // コマンド送信
            DataStructureHelper dataStructureHelper = new DataStructureHelper();
            String data = dataStructureHelper.makeSendData("17","");
            sendFragment.send(data);

            Intent intent = new Intent(this, ProcedureActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        }
    }
}
