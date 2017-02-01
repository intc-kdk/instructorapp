package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
import com.example.masterkdk.methodverification.Util.alertDialogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.masterkdk.methodverification.R.layout.popup_layout;


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

    private String recieveCommand;
    private String recieveData;

    private Button procedureUpdateButton;

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
            System.out.println("!!!!!!!!!!!!!!!!!"+jsonObj.getJSONObject("t_sno").toString());
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

        // 固定のボタンの設定
        procedureUpdateButton = (Button) findViewById(R.id.procedure_update_button);
        procedureUpdateButton.setVisibility(View.INVISIBLE);
        procedureUpdateButton.setOnClickListener(this);
        findViewById(R.id.return_button).setOnClickListener(this);
        findViewById(R.id.site_difference_button).setOnClickListener(this);

        this.recieveCommand = resultArr[0];
        this.recieveData = resultArr[1];

        // 確認待機中の場合、確認を受信できるようにフラグを変更　2016/12/15
        int cpos = mProcFragment.getCurrentPos();
        String scpos = String.valueOf(cpos);
        try {
            JSONObject jsonObj = new JSONObject(resultArr[1]);
            JSONArray js = jsonObj.getJSONArray("tejun");
            JSONObject target =(JSONObject)js.get(cpos);
            String cds = target.getString("cd_status");

            if ( cds.equals("1")) {
                setOrderStatus = 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 現場差異の確認待機時はポップアップ再現
        String cdGsmodeTemp = "";
        try {
            JSONObject jsonObj = new JSONObject(resultArr[1]);
            cdGsmodeTemp = jsonObj.getJSONObject("t_sno").getString("cd_gsmode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String cdGsmode = cdGsmodeTemp;  // Handler内部メソッドに渡す為、final変数へ渡す
        Handler handler = new Handler();
        handler.post(new Runnable(){  // ポップアップはactivity初期化後()の表示でないとエラー
            @Override
            public void run(){
                if (cdGsmode.equals("5") || cdGsmode.equals("6")) {
                    // 現場差異ボタンタップ
                    findViewById(R.id.site_difference_button).callOnClick();
                    // 各差異ボタンのタップ状態再現
                    Button gSButton;
                    View popUp = mPopupWindow.getContentView();
                    int colorNum = getResources().getColor(R.color.colorGrayButton);
                    if (cdGsmode.equals("5")) {
                        diffFlag = 1;
                        popUp.findViewById(R.id.procedure_skip_button).setBackgroundColor(colorNum);
                    } else if(cdGsmode.equals("6")) {
                        diffFlag = 2;
                        popUp.findViewById(R.id.procedure_add_button).setBackgroundColor(colorNum);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    // ボタン(固定)クリック時詳細処理
    private boolean buttonLock = false;  // ボタンをロックするフラグ
    private boolean updateButtonLock = false;  // 画面更新ボタンをロックするフラグ
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

        } else if (id == R.id.return_button && !buttonLock) {  // MENUへ戻るボタン 確認待機中は遷移させない
            // Activity遷移前にrecieveFragment停止
            sendFragment.halt("99@$");
        } else if (id == R.id.procedure_update_button && !updateButtonLock) {  // 画面更新ボタン
            sendFragment.send("90@$");
            this.updateButtonLock = true;
        }
    }

    // ポップアップの初期化
    private PopupWindow mPopupWindow;
    private int diffFlag = 0;  // 現場差異フラグ

    public void onClickSiteDiffButton(View v) {

        this.diffFlag = 0;  // 現場差異フラグの初期化は、キャンセル後の確認の受信に備えて、ポップアップ表示時に行う
        mPopupWindow = new PopupWindow(ProcedureActivity.this);

        // レイアウト設定
        final View popupView = getLayoutInflater().inflate(popup_layout, null);

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
                updateButtonLock = false;
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
                if (diffFlag != 2) {  // 連打対策
                    diffFlag = 2;
                    String commandString = commandBasic + diffFlag + "\"}";
                    String data = dsHelper.makeSendData("14", commandString);
                    sendFragment.send(data);
                    updateButtonLock = false;
                }
            }
        });
        popupView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // キャンセル
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

                if (diffFlag > 0) {  // サーバへ現場差異を指示していたらキャンセルを通知
                    String commandString = commandBasic + "0\"}";
                    String data = dsHelper.makeSendData("14", commandString);
                    sendFragment.send(data);
                }

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

    private int setOrderStatus = 0;
    @Override
    public void onListItemClick(ProcItem item){  // 指示ボタンタップ時の詳細処理

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

        this.buttonLock = true;
        this.updateButtonLock = false;
        this.setOrderStatus = 1;
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {

        System.out.println("CLICK!:" + data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if(cmd.equals("54")) { // 指示が確認者タブレットに伝わった
            // 画面全体の着色
            Resources resources = getResources();
            int instructDisplayColor = resources.getColor(R.color.colorInstructDisplay);
            View wrapProcedure = findViewById(R.id.WrapProcedure);
            wrapProcedure.setBackgroundColor(instructDisplayColor);
        }else if(cmd.equals("50")){
            // do nothing
        }else if(cmd.equals("5R")){
            // 手順番号不一致時に指示を再送
            String correct_sno = dsHelper.getRecievedData().getString("in_sno");
            String reSendData;
            if (this.diffFlag == 0) {
                reSendData = dsHelper.makeSendData("13","{\"手順書番号\":\"" + correct_sno + "\"}");
            } else {
                reSendData = dsHelper.makeSendData("14","{\"in_sno\":\"" + correct_sno + "\",\"Com\":\"" + this.diffFlag + "\"}");
            }
            sendFragment.send(reSendData);

        } else if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, sendFragment, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if(cmd.equals("99")){
            // 待ち受けを中止する
            recieveFragment.closeServer();
            // 次の画面へ遷移
            Intent intent = new Intent(this, TopActivity.class);
            String resultStTmp = this.recieveCommand + "@" + this.recieveData;
            intent.putExtra("resultStTmp", resultStTmp);
            startActivity(intent);
        } else if (cmd.equals("9N")) {  // 画面更新（正常）
            this.updateButtonLock = false;
        } else if (cmd.equals("9Q")) {  // 画面更新（異常）
            this.updateButtonLock = false;
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 送信処理終了
    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
        System.out.println("ReqRecieved:"+data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData ="";

        // 本コールバックでの描画処理はエラーになる
        if(cmd.equals("55")) { // 確認者タブレットで手順が確認された
            if(this.setOrderStatus == 1){  // 確認待機中でなければ処理させない
                updateJSON(dsHelper.getRecievedData());  // 手順JSONの状態を変更
            }
            mData = dsHelper.makeSendData("50", "");

        } else if(cmd.equals("56")) { // 確認者タブレットが現場差異を確認した
//            if(this.diffFlag != 0) {  // 現場差異の発令がなければ処理させない => キャンセル後に確認を受信する可能性がある
                updateJSON(dsHelper.getRecievedData());  // 手順JSONの状態を変更
//            }
            mData = dsHelper.makeSendData("50", "");

        } else if(cmd.equals("57")) { // 盤タブレットの設置状況が変化した
            mData = dsHelper.makeSendData("50", "");
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

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        procedureUpdateButton.setVisibility(View.INVISIBLE);

        if(cmd.equals("55")) {
            if(this.setOrderStatus == 1){
                // 確認者タブレットで確認後の描画処理
                Resources resources = getResources();
                int instructDisplayColor = resources.getColor(R.color.colorBackGround);
                View wrapProcedure = findViewById(R.id.WrapProcedure);
                wrapProcedure.setBackgroundColor(instructDisplayColor);

                // 手順の表示を更新
                int position = mProcFragment.getCurrentPos();
                int lastInSno = mProcFragment.getLastInSno();
                int currentInSno = mProcFragment.getCurrentInSno();
                ProcItem item = mProcFragment.getCurrentItem(); // 現在の手順の行を取得
                String[] arrDate = dsHelper.getRecievedData().getString("ts_b").split(" ");

                mProcFragment.setProcStatus(position, "7", arrDate[1], item.bo_gs, item.tx_gs);  //  bo_gs、tx_gs は更新済みの行から値を設定

                if (lastInSno != currentInSno) {
                    // 手順を進める
                    mProcFragment.updateProcedure();
                    recieveFragment.listen();  // サーバーからの指示を待機
                } else {
                    // 手順終了後は手順書終了画面へ遷移
                    Intent intent = new Intent(this, ProcedureEndActivity.class);
                    startActivity(intent);
                }

                this.buttonLock = false;
                this.setOrderStatus = 0;
            }

        } else if(cmd.equals("56")) { // 確認者タブレットで現場差異確認後の描画処理
//            if(this.diffFlag != 0) {
            // スキップかつ確認待機中の場合のみ、背景色を戻す
                if (setOrderStatus == 1) {
                    if (diffFlag == 1) {
                        Resources resources = getResources();
                        int instructDisplayColor = resources.getColor(R.color.colorBackGround);
                        View wrapProcedure = findViewById(R.id.WrapProcedure);
                        wrapProcedure.setBackgroundColor(instructDisplayColor);
                    }
                }

                int position = mProcFragment.getCurrentPos();
                String tx_gs = "";
                String status = "";
                // cd_status スキップは"7", 追加は "1"
                if (diffFlag == 1) {
                    status = "7";
                    tx_gs = "スキップ";
                } else if (diffFlag == 2) {
                    status = "1";
                    tx_gs = "追加";
                }

                mProcFragment.setProcStatus(position, status, "", "True", tx_gs);   // 対象のエントリの更新

                if (diffFlag == 1) {  // SKIP
                    mProcFragment.updateProcedure();   // SKIPは次のエントリへ進める
                } else {
                    mProcFragment.addProcedure();   // 追加はそのままの手順
                }

                diffFlag = 0;
                updateButtonLock = true;  // フラグを使わずに現場差異確認を再送信させない為、画面更新ボタンを一旦無効化
                mPopupWindow.dismiss();    // ポップアップを閉じる
                recieveFragment.listen();  // サーバーからの指示を待機
//            }

        } else if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            procedureUpdateButton.setVisibility(View.VISIBLE);
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, null, getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
            procedureUpdateButton.setVisibility(View.VISIBLE);
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        } else if(cmd.equals("99")) { // accept キャンセル
            System.out.println("99受信");
            // ここでは何もせず、応答の"99"受信で処理
        } else {
            //想定外コマンドの時も受信待機は継続
            recieveFragment.listen();
        }
    }

    private void updateJSON(Bundle bdData){
        // 確認された手順のJSONを変更
        try {
            JSONObject rData = new JSONObject(this.recieveData);
            JSONArray tejun = rData.getJSONArray("tejun");

            // 手順の各値を変更(状態以外)
            int currentPos = mProcFragment.getCurrentPos();
            JSONObject targetTejun = (JSONObject) tejun.get(currentPos);
            if (this.diffFlag == 0) {
                String tsB = bdData.getString("ts_b");  // 現場差異無しの場合
                targetTejun.put("ts_b", tsB);
            } else {
                targetTejun.put("bo_gs", "True");  // 現場差異有りの場合
                if (this.diffFlag == 1) {
                    targetTejun.put("tx_gs", "スキップ");
                } else if(this.diffFlag == 2) {
                    targetTejun.put("tx_gs", "追加");
                }
            }
            tejun.put(currentPos, targetTejun);

            // 手順の状態を変更
            if (this.diffFlag == 2) {
                targetTejun.put("cd_status", "1"); // 現場差異"追加"は手順を進めないので確認済にしない
            } else {
                targetTejun.put("cd_status", "7"); // 確認済に変更
                // コメント行は実行済みとして行を進める
                int nextPos = currentPos + 1;
                JSONObject nextTejun = (JSONObject) tejun.get(nextPos);
                while(nextTejun.getString("tx_sno").equals("C")){
                    nextTejun.put("cd_status", "7");
                    tejun.put(nextPos, nextTejun);
                    nextPos++;
                    nextTejun = (JSONObject) tejun.get(nextPos);
                }
                tejun.put(nextPos, nextTejun);
            }
            rData.put("tejun", tejun);

            // 画面全体の着色を戻す
            JSONObject t_sno = rData.getJSONObject("t_sno");
            t_sno.put("cd_status","0");

            this.recieveData = rData.toString() + "$";  // 末尾の"$"はこのタイミングで追加しないと、2回目に戻った時エラー

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void recieveKakunin(Bundle bdData){
        Resources resources = getResources();
        int instructDisplayColor = resources.getColor(R.color.colorBackGround);
        View wrapProcedure = findViewById(R.id.WrapProcedure);
        wrapProcedure.setBackgroundColor(instructDisplayColor);

        // 手順の表示を更新
        int position = mProcFragment.getCurrentPos();
        int lastInSno = mProcFragment.getLastInSno();
        int currentInSno = mProcFragment.getCurrentInSno();
        ProcItem item = mProcFragment.getCurrentItem(); // 現在の手順の行を取得
        String[] arrDate = bdData.getString("ts_b").split(" ");

        mProcFragment.setProcStatus(position, "7", arrDate[1], item.bo_gs, item.tx_gs);  //  bo_gs、tx_gs は更新済みの行から値を設定

        if (lastInSno != currentInSno) {
            // 手順を進める
            mProcFragment.updateProcedure();
        } else {
            // 手順終了後は手順書終了画面へ遷移
            Intent intent = new Intent(this, ProcedureEndActivity.class);
            startActivity(intent);
        }
    }
}
