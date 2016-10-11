package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.masterkdk.methodverification.Helper.DataStructureHelper;

/**
 * Created by masterkdk on 2016/09/26.
 * S-06 終了方法選択画面
 */

public class EndActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String HOST = "192.168.10.20";
    private static final int PORT = 1280;  // ポート(実環境)
//    private static final int PORT = 1234;  // ポート(VisualStudio)
    private static final String TAG_TRANS = "No_UI_Fragment1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // ボタンへリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
//        findViewById(R.id.two_end_button).setOnClickListener(this);
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
        } else {
            // 全てのタブレットを終了する

            // Fragmentを利用した通信の準備
            TransmissionFragment sendFragment = TransmissionFragment.newInstance(HOST,PORT);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(sendFragment, TAG_TRANS);
            transaction.commit();
            fragmentManager.executePendingTransactions();   // 即時実行

            // コマンド送信
            DataStructureHelper dataStructureHelper = new DataStructureHelper();
            String data = dataStructureHelper.makeSendData("16","");
            sendFragment.send(data);

            intent = new Intent(this, EndOffActivity.class);

//            Intent pI = getIntent();
//            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        }
    }
}
