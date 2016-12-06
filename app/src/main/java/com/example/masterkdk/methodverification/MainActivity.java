package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.alertDialogUtil;

/*
 *  データ受信待ち画面
*/

public class MainActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [P] 起動電文を作成
        DataStructureUtil ds = new DataStructureUtil();
        String mData = ds.makeSendData("10","");

        // [P] 起動を通知
        // TransmissionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // [P] 起動通知を送信
        sendFragment.send(mData);
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = (String)dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if (cmd.equals("51")) { //起動応答
            Intent intent = new Intent(this, StatusActivity.class);
            intent.putExtra("responseData", data);
            startActivity(intent);
        } else if (cmd.equals("91")) {  // 受信エラー処理
            System.out.println("※※※※　受信エラー ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            System.out.println("※※※※　受信タイムアウト ※※※"+data);
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 行う処理は無いが、implementsに必要なfunction
    }
}
