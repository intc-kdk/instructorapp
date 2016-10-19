package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.DataStructureUtil.ProcItem;

import java.util.List;
/*
 *  K-02 手順書画面
*/

public class ProcedureActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        ProcedureFragment.OnListFragmentInteractionListener{

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final int REQUEST_CODE_OPERATION = 1;

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;
    private ProcedureFragment mProcFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure);

        //  手順書フラグメントの取得
        mProcFragment = (ProcedureFragment)getSupportFragmentManager()
                .findFragmentById(R.id.ProcedureList);

        // 初回起動時のみ、手順のカレント表示はマニュアル設定
        mProcFragment.setFirstProcedure();

        // TransmissionFragment/ReceptionFragment を　生成
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

        // サーバーからの指示を待機
        recieveFragment.listen();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public void onListFragmentInteraction(Bundle rcBundle) {
        // Fragmentからの通知で、ヘッダの表示を更新する
        if(rcBundle.getString("cd_status").equals("1")) {   // 状態が実行通の時
            TextView tvNo = (TextView) findViewById(R.id.title_proc_no);
            TextView tvPlace = (TextView) findViewById(R.id.title_proc_place);
            TextView tvAction = (TextView) findViewById(R.id.title_proc_action);
            TextView tvRemarks = (TextView) findViewById(R.id.title_proc_remarks);

            tvNo.setText(rcBundle.getString("tx_sno"));
            tvPlace.setText(rcBundle.getString("tx_s_l"));
            tvAction.setText(rcBundle.getString("tx_action"));
            tvRemarks.setText(rcBundle.getString("tx_biko"));
        }
    }
    @Override
    public void onListItemClick(ProcItem item){

        // 指示ボタンタップ時の詳細処理
        System.out.println("CLICK!:"+item.tx_sno);

        // ヘッダへの値表示(No、盤・機器名、指示名)
        TextView tvNo = (TextView) findViewById(R.id.title_proc_no);
        TextView tvPlace = (TextView) findViewById(R.id.title_proc_place);
        TextView tvAction = (TextView) findViewById(R.id.title_proc_action);
        TextView tvRemarks = (TextView) findViewById(R.id.title_proc_remarks);
        tvNo.setText(item.tx_sno);
        tvPlace.setText(item.tx_s_l);
        tvAction.setText(item.tx_action);
        tvRemarks.setText(item.tx_biko);

        // コマンド送信
        DataStructureUtil dsHelper = new DataStructureUtil();
        String data = dsHelper.makeSendData("13","{\"手順書番号\":\"" + item.tx_sno + "\"}");
        sendFragment.send(data);

//        item.
    }

    private void setProcActivate(){
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        // TODO: [P] ログを取得
        System.out.println("CLICK!:" + data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if(cmd.equals("50")) { // 指示が確認者タブレットに伝わった
            // 選択行の着色と指示ボタンの無効化
            Resources res = getResources();
            int lockColor = res.getColor(R.color.colorYellowButton);
//        item.setBackgroundColor(lockColor);
//        item.setEnabled(false);
//            mProcFragment.setA
        }

        // サーバーからの指示を待機
        recieveFragment.listen();
    }

    @Override
    public void onFinishTransmission(String data){
        // 送信処理終了

    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
        //System.out.println("ReqRecieved:"+data);
        DataStructureUtil dsHelper = new DataStructureUtil();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
//        Bundle bdRecievedData = dsHelper.getRecievedData();  // 渡したデータを解析し、Bundleを返す

        if(cmd.equals("55")) { // 確認者タブレットで確認されたことを通知
            System.out.println("CLICK!:" + data);
//            mProcFragment.updateProcedure();  // エラー発生
        }
/*
        if(cmd.equals("63")) { //指示命令
            if (bdRecievedData.getString("format").equals("TEXT")) {
                // 操作対象を取得
                List<ProcItem> item = mProcFragment.getCurrentProcedure();

                Bundle bdCur = new Bundle();

                bdCur.putInt("in_sno", item.get(0).in_sno);
                bdCur.putString("tx_sno", item.get(0).tx_sno);
                bdCur.putString("tx_s_l", item.get(0).tx_s_l);
                bdCur.putString("tx_action", item.get(0).tx_action);
                bdCur.putString("tx_b_l", item.get(0).tx_b_l);
                bdCur.putString("tx_b_r", item.get(0).tx_b_r);
                bdCur.putString("tx_clr1", item.get(0).tx_clr1);
                bdCur.putString("tx_clr2", item.get(0).tx_clr2);
                bdCur.putString("tx_biko", item.get(0).tx_biko);
                bdCur.putString("cd_status", item.get(0).cd_status);

                Bundle bdPair = new Bundle();

                bdPair.putInt("in_sno", item.get(1).in_sno);
                bdPair.putString("tx_sno", item.get(1).tx_sno);
                bdPair.putString("tx_s_l", item.get(1).tx_s_l);
                bdPair.putString("tx_action", item.get(1).tx_action);
                bdPair.putString("tx_b_l", item.get(1).tx_b_l);
                bdPair.putString("tx_b_r", item.get(1).tx_b_r);
                bdPair.putString("tx_clr1", item.get(1).tx_clr1);
                bdPair.putString("tx_clr2", item.get(1).tx_clr2);
                bdPair.putString("cd_status", item.get(1).cd_status);


                //Intent生成
                Intent intent = new Intent(this, OperationActivity.class);

                intent.putExtra("current", bdCur);
                intent.putExtra("pair", bdPair);

                //盤操作画面を起動
                startActivityForResult(intent, REQUEST_CODE_OPERATION);
            }
        }
*/
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // 盤操作画面からの戻り
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;
        Bundle resultBundle = data.getExtras();

        if(!resultBundle.containsKey("in_sno")) return;
        String status = resultBundle.getString("status");
        int in_sno = resultBundle.getInt("in_sno");

        if(requestCode == REQUEST_CODE_OPERATION) {
            // 該当操作のステータスを更新
            int position = mProcFragment.getCurrentPos();

            mProcFragment.setProcStatus(position, status);   // 対象のエントリの更新

            if(mProcFragment.getLastInSno() > in_sno) {
                mProcFragment.updateProcedure();                 // 次のエントリへ進める

                // サーバーからの指示を待機
                recieveFragment.listen();
            }else{
                // 最終手順の時、終了画面表示
                Intent intent = new Intent(this,EndActivity.class);
                startActivity(intent);

            }

        }
    }
    @Override
    public void onFinishRecieveProgress() {
        // コマンド送受信後の 次への処理判定
    }

}
