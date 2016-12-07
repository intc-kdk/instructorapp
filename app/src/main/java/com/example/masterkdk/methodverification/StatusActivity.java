package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v7.app.AppCompatActivity;

import com.example.masterkdk.methodverification.Util.DataStructureUtil;
import com.example.masterkdk.methodverification.Util.alertDialogUtil;


public class StatusActivity extends AppCompatActivity
        implements OnClickListener, TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    private String nextActivity = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // 通信準備
        sendFragment = TransmissionFragment.newInstance();
        recieveFragment = ReceptionFragment.newInstance();

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(sendFragment, TAG_TRANS);
        transaction.add(recieveFragment, TAG_RECEP);
        transaction.commit();

        fragmentManager.executePendingTransactions();  // 即時実行
        recieveFragment.listen();  // サーバーからの指示を待機

        // ボタンへリスナを登録
        findViewById(R.id.menu_button).setOnClickListener(this);
        findViewById(R.id.update_button).setOnClickListener(this);
        findViewById(R.id.start_button).setOnClickListener(this);

        Intent intnt = getIntent();
        String data = intnt.getStringExtra("resultStTmp");
        try {
            // 盤タブレット設置状況の表示
            String[] tabletTableItem = {"名称", "状況"};  // タブレット設置状況の項目
            String[] resultArr = data.split("@");  // 受信値:コマンド@表示値(JSON)
            JSONObject responseJson = new JSONObject(resultArr[1]);
            JSONArray tabletArray = responseJson.getJSONArray("tablet");
            TableLayout tableTablet = (TableLayout) findViewById(R.id.table_tablet);
            TableRow.LayoutParams columnLayout = new TableRow.LayoutParams();  // 通常の行のレイアウトパラメータ
            columnLayout.setMargins(2, 2, 2, 2);  // 動的なスタイル設定はstyles.xmlに書いたmarginを利用できない
            TableRow addRow = null;
            TextView addColumn = null;
            String innerText = null;
            int columnNum = tabletTableItem.length;  // タブレット設置状況の列数
            // 行の追加
            for (int i = 0; i < tabletArray.length(); i++) {
                addRow = new TableRow(this);

                // 列を左から追加
                for (int j = 0; j < columnNum; j++) {
                    innerText = tabletArray.getJSONObject(i).getString(tabletTableItem[j]);
                    addColumn = new TextView(this, null, R.attr.S01TabletTableColumnDynamic);
                    addColumn.setText(innerText);
                    addRow.addView(addColumn, columnLayout);
                }

                tableTablet.addView(addRow);
            }

            // 手順一覧の表示
            JSONArray procedureArray = responseJson.getJSONArray("tejun");
            TableLayout tableProcedure = (TableLayout) findViewById(R.id.table_procedure);

            String[] procedureTableItem = {"tx_sno", "tx_basho", "tx_bname", "tx_swname", "tx_action", "tx_biko", "dotime", "tx_gs"};  // 手順一覧の項目(コメント除く)
            columnNum = procedureTableItem.length;  // 手順一覧の列数
            // コメント行のレイアウトパラメータ
            TableRow.LayoutParams commentColumnLayout = new TableRow.LayoutParams();
            commentColumnLayout.setMargins(2, 2, 2, 2);
            commentColumnLayout.span = columnNum;
            String tableItem = null;

            // 行の追加
            TableRow tr = null;
            int rowNum = procedureArray.length();
            for (int i=0; i < rowNum; i++) {
                // tx_sno を取得
                String tx_sno = procedureArray.getJSONObject(i).getString("tx_sno");

                int row = i+1;
                if(tx_sno.equals("C")){
                    // コメント行の追加
                    getLayoutInflater().inflate(R.layout.procedure_comment_row, tableProcedure);
                    tr = (TableRow)tableProcedure.getChildAt(row);
                    innerText = procedureArray.getJSONObject(i).getString("tx_com");
                    ((TextView) (tr.getChildAt(0))).setText(innerText);
                }else {
                    // 通常行の追加
                    getLayoutInflater().inflate(R.layout.procedure_row, tableProcedure);
                    tr = (TableRow) tableProcedure.getChildAt(row);

                    // 対象フィールドの値でカラムを埋める
                    for (int j = 0; j < columnNum; j++) {
                        tableItem = procedureTableItem[j];
                        innerText = procedureArray.getJSONObject(i).getString(tableItem);
                        ((TextView) (tr.getChildAt(j))).setText(innerText);
                    }
                }
            }

            // 手順書の保存
            this.resultStTmp = data;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ボタンクリック時詳細処理
    private boolean updateButtonLock = false;  // 画面更新ボタンをロックするフラグ
    @Override
    public void onClick(View v){

        // recieveFragment停止後にActivity遷移させる為、ここではActivity種別の指定のみ行う
        int id = v.getId();
        if (id == R.id.menu_button) {
            nextActivity="top";
            sendFragment.halt("99@$");
        } else if (id == R.id.start_button) {
            nextActivity="conf";
            sendFragment.halt("99@$");
        } else if (id == R.id.update_button && !updateButtonLock) {
            sendFragment.send("90@$");
            this.updateButtonLock = true;
        }
    }

    /* 応答受信 */
    private String resultStTmp = null;
    @Override
    public void onResponseRecieved(String data)  {
        System.out.println("CLICK!:" + data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if (cmd.equals("91")) {  // 受信エラー処理
            alertDialogUtil.show(this, sendFragment ,getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if (cmd.equals("92")) {  // タイムアウト
            alertDialogUtil.show(this, sendFragment ,getResources().getString(R.string.nw_err_title),getResources().getString(R.string.nw_err_message));
        } else if(cmd.equals("99")){
            // 待ち受けを中止する
            recieveFragment.closeServer();
            // 次の画面へ遷移
            Intent intent = null;
            if(nextActivity.equals("top")){
                intent = new Intent(this, TopActivity.class);
            }else{
                intent = new Intent(this, ConfirmActivity.class);
            }
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
        // インターフェイス利用に必要なオーバーライド
    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data){
        // サーバーからの要求（data）を受信
        System.out.println("ReqRecieved:"+data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        String mData ="";

        if(cmd.equals("57")) { // 盤タブレットの設置状況が更新された
            // 非同期処理と表示更新のタイミングの都合により、実際の処理はonFinishRecieveProgressで行う
            System.out.println("CLICK!:" + data);
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
        // コマンド送受信後の 次への処理判定

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if(cmd.equals("57")) { // 盤タブレット更新後の描画
            System.out.println("57受信");
            // 設置状況テーブルの更新
            TableLayout tabletTable = (TableLayout) findViewById(R.id.table_tablet);
            int tabletTableRowNum = tabletTable.getChildCount();
            tabletTable.removeViews(1, tabletTableRowNum - 1);
            try {
                // 盤タブレット設置状況の表示
                String[] tabletTableItem = {"名称", "状況"};  // タブレット設置状況の項目
                JSONObject responseJson = new JSONObject(data.split("@")[1]);
                JSONArray tabletArray = responseJson.getJSONArray("tablet");
                TableRow.LayoutParams columnLayout = new TableRow.LayoutParams();  // 通常の行のレイアウトパラメータ
                columnLayout.setMargins(2, 2, 2, 2);  // 動的なスタイル設定はstyles.xmlに書いたmarginを利用できない
                TableRow addRow = null;
                TextView addColumn = null;
                String innerText = null;
                int columnNum = tabletTableItem.length;  // タブレット設置状況の列数
                // 行の追加
                for (int i = 0; i < tabletArray.length(); i++) {
                    addRow = new TableRow(this);

                    // 列を左から追加
                    for (int j = 0; j < columnNum; j++) {
                        innerText = tabletArray.getJSONObject(i).getString(tabletTableItem[j]);
                        addColumn = new TextView(this, null, R.attr.S01TabletTableColumnDynamic);
                        addColumn.setText(innerText);
                        addRow.addView(addColumn, columnLayout);
                    }

                    tabletTable.addView(addRow);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            recieveFragment.listen();  // サーバーからの指示を待機

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
