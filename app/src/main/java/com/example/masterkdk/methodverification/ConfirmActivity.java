package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.alertDialogUtil;

import java.util.Iterator;


/**
 * Created by masterkdk on 2016/09/23.
 * S-03 確認画面
 */

public class ConfirmActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener,
        ReceptionFragment.ReceptionFragmentListener{

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECV = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String dtKakunin;   // 55@退避変数
    private String dtGenbasai;  // 56@退避変数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        //退避エリアを初期化
        dtKakunin = "";
        dtGenbasai = "";

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
        findViewById(R.id.ok_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.ok_button) {

            // OKコマンド送信
            DataStructureUtil dataStructureHelper = new DataStructureUtil();
            String data = dataStructureHelper.makeSendData("17","");
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

        if (cmd.equals("50")) {
            // 応答が正常終了だったら Activity遷移前にrecieveFragment停止
            sendFragment.halt("99@$");

        } else if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("99")) {
            // 待ち受けを中止する
            recieveFragment.closeServer();
            // 次の画面へ遷移
            Intent intent = new Intent(this, ProcedureActivity.class);

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
