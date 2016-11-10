package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.DataStructureUtil.ProcItem;

import org.json.JSONException;
import org.json.JSONObject;


//import java.util.List;
/*
 *  K-02 手順書画面
*/

public class ProcedureActivity extends AppCompatActivity
        implements TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener,
        ProcedureFragment.OnListFragmentInteractionListener, View.OnClickListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

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

        // 指示後の確認待機中であれば画面着色
        Intent intnt = getIntent();
        String intntString = intnt.getStringExtra("resultStTmp");
        String[] resultArr = intntString.split("@");
        String cdStatus = "";
        try {
            JSONObject jsonObj = new JSONObject(resultArr[1]);
            cdStatus = jsonObj.getJSONObject("t_sno").getString("cd_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(cdStatus.equals("1")) {
            // 画面全体の着色
            Resources resources = getResources();
            int instructDisplayColor = resources.getColor(R.color.colorInstructDisplay);
            View wrapProcedure = findViewById(R.id.WrapProcedure);
            wrapProcedure.setBackgroundColor(instructDisplayColor);
        }

        // ボタン(固定)へリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
        findViewById(R.id.site_difference_button).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    // ボタン(固定)クリック時詳細処理
    @Override
    public void onClick(View v) {

        int id = v.getId();
        Intent intent = null;

        if (id == R.id.site_difference_button) {  // 現場差異ボタン

            // 表示切替
            Drawable backgroundPicture = getResources().getDrawable(R.drawable.bg_diff_off);
            v.setBackground(backgroundPicture);
            Button btn = (Button) v;
            Resources res = getResources();
            btn.setTextColor(res.getColor(R.color.colorTextLightGray));

            this.onClickSiteDiffButton(v);

        } else if (id == R.id.return_button) {    // MENUへ戻るボタン
            intent = new Intent(this, TopActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        }
    }

    // ポップアップの初期化
    private PopupWindow mPopupWindow;
    private int diffFlag = 0;  // 現場差異フラグ

    public void onClickSiteDiffButton(View v) {

        mPopupWindow = new PopupWindow(ProcedureActivity.this);

        // レイアウト設定
        final View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // ボタン設定
        final DataStructureUtil dsHelper = new DataStructureUtil();
        final String currentInSno = Integer.toString(mProcFragment.getCurrentInSno());
        final String commandBasic = "{\"in_sno\":\"" + currentInSno + "\",\"Com\":\"";
        popupView.findViewById(R.id.procedure_skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順をスキップする
                // ボタン切替
                int colorNum = getResources().getColor(R.color.colorGrayButton);
                v.setBackgroundColor(colorNum);
                if (diffFlag > 0) {
                    int anotherColorNum = getResources().getColor(R.color.colorYellowButton);
                    View anotherV = popupView.findViewById(R.id.procedure_add_button);
                    anotherV.setBackgroundColor(anotherColorNum);
                }
                // コマンド送信
                diffFlag = 1;
                String commandString = commandBasic + diffFlag +"\"}";
                String data = dsHelper.makeSendData("14", commandString);
                sendFragment.send(data);
            }
        });
        popupView.findViewById(R.id.procedure_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順の前に操作を追加する
                // ボタン切替
                int colorNum = getResources().getColor(R.color.colorGrayButton);
                v.setBackgroundColor(colorNum);
                if (diffFlag > 0) {
                    int anotherColorNum = getResources().getColor(R.color.colorYellowButton);
                    View anotherV = popupView.findViewById(R.id.procedure_skip_button);
                    anotherV.setBackgroundColor(anotherColorNum);
                }
                // コマンド送信
                diffFlag = 2;
                String commandString = commandBasic + diffFlag +"\"}";
                String data = dsHelper.makeSendData("14", commandString);
                sendFragment.send(data);
            }
        });
        popupView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // キャンセル
/*
                if (diffFlag > 0) {  // サーバへ現場差異を指示していたらキャンセルを通知
                    String commandString = commandBasic + "0\"}";
                    String data = dsHelper.makeSendData("14", commandString);
                    sendFragment.send(data);
                }
                diffFlag = 0;
*/
                mPopupWindow.dismiss();  // ポップアップ削除
            }
        });

        mPopupWindow.setContentView(popupView);

        // 背景設定
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));

        // タップ時に他のViewでキャッチされないための設定
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        // 表示サイズの設定 今回は幅450dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
        mPopupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setWidth((int) width);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // 現場差異ボタンの行に表示
        mPopupWindow.showAtLocation(findViewById(R.id.site_difference_button), Gravity.NO_GRAVITY, 300, 250);

        // 手順書画面フッタにメッセージを表示
        final TextView siteDifferenceText = (TextView) findViewById(R.id.site_diff_text);
        String siteDifferenceTextString = getString(R.string.S_05_site_difference_text);
        siteDifferenceText.setText(siteDifferenceTextString);

        // ポップアップ削除時の処理
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 本コールバックは、キャンセルボタンだけでなく、ポップアップ外タップにも対応
/*
                if (diffFlag > 0) {  // サーバへ現場差異を指示していたらキャンセルを通知
                    String commandString = commandBasic + "0\"}";
                    String data = dsHelper.makeSendData("14", commandString);
                    sendFragment.send(data);
                }
                diffFlag = 0;
*/
                // ボタン表示切替
                Button siteDiffButton = (Button) findViewById(R.id.site_difference_button);
                Drawable backgroundPicture = getResources().getDrawable(R.drawable.bg_diff_on);
                siteDiffButton.setBackground(backgroundPicture);
                Resources res = getResources();
                siteDiffButton.setTextColor(res.getColor(R.color.colorTextBlack));

                siteDifferenceText.setText("");  // 画面フッタ文言削除
            }
        });
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
        System.out.println("CLICK!:"+item.in_sno);

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
        String data = dsHelper.makeSendData("13","{\"手順書番号\":\"" + item.in_sno + "\"}");
        sendFragment.send(data);
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {
        // TODO: [P] ログを取得
        System.out.println("CLICK!:" + data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

//        if(cmd.equals("50")) { // 指示が確認者タブレットに伝わった
        if(cmd.equals("50") && diffFlag == 0) { // 指示が確認者タブレットに伝わった
            // 画面全体の着色
            Resources resources = getResources();
            int instructDisplayColor = resources.getColor(R.color.colorInstructDisplay);
            View wrapProcedure = findViewById(R.id.WrapProcedure);
            wrapProcedure.setBackgroundColor(instructDisplayColor);
            // ボタンの無効化はRecyclerViewAdapterで行う
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 送信処理終了

    }

    /* 要求受信 */
    private String recievedCmd = "";      // コマンド受渡変数
    private Bundle recievedParam = null;  // パラメータ受渡変数
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
        System.out.println("ReqRecieved:"+data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData ="";

        // 本コールバックでの描画処理はエラーになる
        if(cmd.equals("55")) { // 確認者タブレットで手順が確認された

            System.out.println("CLICK!:" + data);
            recievedCmd = cmd;
            recievedParam = dsHelper.getRecievedData();
            mData = dsHelper.makeSendData("50", "");

        } else if(cmd.equals("56")) { // 確認者タブレットが現場差異を確認した
            recievedCmd = cmd;
            mData = dsHelper.makeSendData("50", "");
        }

        return mData;
    }

    @Override
    public void onFinishRecieveProgress() {

        if(recievedCmd.equals("55")) { // 確認者タブレットで確認後の描画処理
            // 確認待機中の着色の解除
            Resources resources = getResources();
            int instructDisplayColor = resources.getColor(R.color.colorBackGround);
            View wrapProcedure = findViewById(R.id.WrapProcedure);
            wrapProcedure.setBackgroundColor(instructDisplayColor);

            // 手順の表示を更新
            int position = mProcFragment.getCurrentPos();
            int lastInSno = mProcFragment.getLastInSno();
            int currentInSno = mProcFragment.getCurrentInSno();
            String[] arrDate = recievedParam.getString("ts_b").split(" ");
//            mProcFragment.setProcStatus(position, "7", arrDate[1]);
            mProcFragment.setProcStatus(position, "7", arrDate[1], "False", "");

//            if (lastInSno > position + 1) {
            if (lastInSno != currentInSno) {
                // 手順を進める
                mProcFragment.updateProcedure();
                recieveFragment.listen();  // サーバーからの指示を待機
            } else {
                // 最後の手順の場合、手順を進めずに表示のみ更新
                mProcFragment.updateLastProcedure();
            }
//        }
        } else if(recievedCmd.equals("56")) { // 確認者タブレットで現場差異確認後の描画処理

            // ポップアップを閉じる
            mPopupWindow.dismiss();

            // TODO:手順の表示を更新
            int position = mProcFragment.getCurrentPos();
            String tx_gs = "";
            String status = "";
            // cd_status スキップは"7", 追加は "0"
            if(diffFlag == 1){
                status="7";
                tx_gs="スキップ";
            }else if(diffFlag == 2){
                status="0";
                tx_gs="追加";
            }
            mProcFragment.setProcStatus(position, status, "", "True", tx_gs);   // 対象のエントリの更新
            // TODO: 最終エントリの判定要

            if(diffFlag == 1) {  // SKIP
                mProcFragment.updateProcedure();   // SKIPは次のエントリへ進める
            }else{
//                mProcFragment.addProcedure();   // 追加はそのままの手順
            }

            diffFlag = 0;
            recieveFragment.listen();  // サーバーからの指示を待機

        } else {
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        }
    }
}
