package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.alertDialogUtil;

import java.util.Iterator;

/**
 * Created by masterkdk on 2016/09/21.
 * S-02 トップページ
 */

public class TopActivity extends AppCompatActivity implements View.OnClickListener, ReceptionFragment.ReceptionFragmentListener,
        TransmissionFragment.TransmissionFragmentListener{

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECV = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String dtKakunin;   // 55@退避変数
    private String dtGenbasai;  // 56@退避変数
    private String nextActivity; // 次の画面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);
        //退避エリアを初期化
        dtKakunin = "";
        dtGenbasai = "";
        nextActivity = "";
System.out.println("スタート");
        // Fragmentを利用した通信の準備
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();
        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECV);
        transaction.commit();

        fragmentManager.executePendingTransactions();   // 即時実行
        recieveFragment.listen();   // サーバーからの指示を待機

        // ボタンへリスナを登録
        findViewById(R.id.start_button).setOnClickListener(this);
        findViewById(R.id.end_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){

        int id = v.getId();
        Intent intent = null;

        if (id == R.id.start_button) {
            nextActivity = "start";   // 確認画面へ
        } else if (id == R.id.end_button) {
            nextActivity = "end";  //  終了画面へ
        } else if (id == R.id.confirm_button) {
            nextActivity = "conf";  // 設置状況、手順一覧へ
        }
        sendFragment.halt("99@$"); // 受信停止コマンド送信
    }

    // サーバからの応答受信
    @Override
    public void onResponseRecieved(String data) {
        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("99")) {
            // 待ち受けを中止する
            recieveFragment.closeServer();
            // 次の画面へ遷移
            Intent intent = null;
            if(nextActivity.equals("start")){   // 確認画面
                intent = new Intent(this, ConfirmActivity.class);
            }else if(nextActivity.equals("end")){   //  終了画面
                intent = new Intent(this, EndActivity.class);
            }else if(nextActivity.equals("conf")){  // 設置状況、手順一覧
                intent = new Intent(this, MainActivity.class);
            }

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));
            if(!dtKakunin.isEmpty()){
                // 確認時刻を受信済みの場合、Intentへ設定 ※ この画面での受信（最新）を優先する
                intent.putExtra("dtKakunin", dtKakunin);
            }else{
                // 引き継いだIntentに確認時刻を受信が設定済みの場合
                if( searchIntent(pI.getExtras(),"dtKakunin")){
                    intent.putExtra("dtKakunin", pI.getStringExtra("dtKakunin"));
                }
            }
            if(!dtGenbasai.isEmpty()){
                // 現場差異応答を受信済みの場合、Intentへ設定
                intent.putExtra("dtGenbasai", dtGenbasai);
            }else {
                // 引き継いだIntentに現場差異応答を受信が設定済みの場合
                if (searchIntent(pI.getExtras(),"dtGenbasai")) {
                    intent.putExtra("dtGenbasai", pI.getStringExtra("dtGenbasai"));
                }
            }
            startActivity(intent);
        }
    }
    private boolean searchIntent(Bundle bd, String key){
        // インテントに該当のキーがあるか確認する
        if (bd != null) {
            Iterator<?> iterator = bd.keySet().iterator();
            while (iterator.hasNext()) {
                String inKey = (String) iterator.next();
                if(inKey.equals(key)){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void onFinishTransmission(String data){
        // インターフェイス利用に必要なオーバーライド
    }
    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData ="";
        if(cmd.equals("55")) { // 確認時刻受信
            mData = dsHelper.makeSendData("50","");
        }else if(cmd.equals("56")) { // 現場差異応答
            mData = dsHelper.makeSendData("50","");
        }else if(cmd.equals("57")) { // 盤タブレットの設置状況が更新された
            // 非同期処理と表示更新のタイミングの都合により、実際の処理はonFinishRecieveProgressで行う
            System.out.println("CLICK!:" + data);
            mData = dsHelper.makeSendData("50","");
        } else if (cmd.equals("91")) {  // 受信エラー処理 onFinishRecieveProgress で処理
            mData = "";
        } else if (cmd.equals("92")) {  // タイムアウト onFinishRecieveProgress で処理
            mData = "";
        } else if(cmd.equals("99")) {
            mData = dsHelper.makeSendData("99","");
        }

        return mData;
    }

    @Override
    public void onFinishRecieveProgress(String data) {
        // サーバー発呼コマンド送受信後の処理
        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if(cmd.equals("55")) { // 確認時刻受信
            dtKakunin = data;  // データを退避
            recieveFragment.listen(); // 受信待機を継続する
        }else if(cmd.equals("56")) { // 現場差異応答
            dtGenbasai=data;   // データを退避
            recieveFragment.listen(); // 受信待機を継続する
        }else if(cmd.equals("57")) { // 盤タブレットの設置状況が更新された
            // 盤タブレットの設置状況は引き継ぎは行わないため、退避しない
            recieveFragment.listen(); // 受信待機を継続する
        } else if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if(cmd.equals("99")) { // accept キャンセル
            System.out.println("99受信");
            // ここでは何もせず、応答の"99"受信で処理
        }
    }
}
