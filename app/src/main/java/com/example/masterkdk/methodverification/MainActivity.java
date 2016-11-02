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


public class MainActivity extends AppCompatActivity
        implements OnClickListener, TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener {

    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    private TransmissionFragment sendFragment;
    private ReceptionFragment recieveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // サーバへ指示者アプリの活性(コマンド:10)を送信
        DataStructureUtil dataStructureHelper = new DataStructureUtil();
        String data = dataStructureHelper.makeSendData("10","");
        sendFragment.send(data);

        // ボタンへリスナを登録
        findViewById(R.id.menu_button).setOnClickListener(this);
        findViewById(R.id.start_button).setOnClickListener(this);
    }

    private String resultStTmp = null;

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        Intent intent = null;
        if (id == R.id.menu_button) {
            intent = new Intent(this, TopActivity.class);

            intent.putExtra("resultStTmp", resultStTmp);

            startActivity(intent);
        } else if (id == R.id.start_button) {
            intent = new Intent(this, ConfirmActivity.class);

            intent.putExtra("resultStTmp", resultStTmp);

            startActivity(intent);
        }
    }

    /* 応答受信 */
    @Override
    public void onResponseRecieved(String data)  {

        System.out.println("CLICK!:" + data);

        DataStructureUtil dsHelper = new DataStructureUtil();
        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if(cmd.equals("51")) { // 活性が伝わり、設置状況と手順のリストを受信
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
//            StringBuilder stringBuilder = null;
                String tableItem = null;
                int newLineTargetNum = 0;
                // 行の追加
                for (int i = 0; i < procedureArray.length(); i++) {
                    addRow = new TableRow(this);
                    // 列を左から追加
                    for (int j = 0; j < columnNum; j++) {
                        tableItem = procedureTableItem[j];
                        innerText = procedureArray.getJSONObject(i).getString(tableItem);
                        if (tableItem.equals("tx_sno") && innerText.equals("C")) {
                            // Noが"C"の場合はコメント行を追加
                            innerText = procedureArray.getJSONObject(i).getString("tx_com");
                            addColumn = new TextView(this, null, R.attr.S01ProcedureTableCommentColumnDynamic);
                            addColumn.setText(innerText);

//                        addColumn.setWidth(200);

                            addRow.addView(addColumn, commentColumnLayout);
                            break;
                        } else {
                            // 通常の行の追加
                            addColumn = new TextView(this, null, R.attr.S01ProcedureTableColumnDynamic);

                            // テーブル幅の伸長を抑止する
//                        addColumn.setWidth(150);  // 150は暫定値。これでTableLayoutの幅の"wrap_parent"を解除して数値指定で固定すればいける？？

                            addColumn.setText(innerText);
                            addRow.addView(addColumn, columnLayout);
                        }
                    }

//                addRow.setMinimumWidth(100);

                    tableProcedure.addView(addRow);
                }
/*
            // 行レイアウトの追加
            for (int i=0; i < 3; i++) {
                // 行を追加
                getLayoutInflater().inflate(R.layout.location_row, tableProcedure);
            }
*/
//            ViewGroup.LayoutParams lp = tableProcedure.getLayoutParams();
//            lp.width = 790;
//            lp.width = "";
//                tableProcedure.setLayoutParams(lp);

                // 手順書の保存
//            this.resultStTmp = resultArr[1];
                this.resultStTmp = data;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // サーバーからの指示を待機
        recieveFragment.listen();
    }

    @Override
    public void onFinishTransmission(String data){
        // インターフェイス利用に必要なオーバーライド
    }

    /* 要求受信 */
    private String recievedCmd = "";    // コマンド受渡変数
    private String recievedParam = "";  // パラメータ受渡変数
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
            recievedCmd = cmd;
            recievedParam = data.split("@")[1];
            mData = dsHelper.makeSendData("50","");
        }

        return mData;
    }

    @Override
    public void onFinishRecieveProgress() {
        // コマンド送受信後の 次への処理判定

        if(recievedCmd.equals("57")) { // 盤タブレット更新後の描画

            // 設置状況テーブルの更新
            TableLayout tabletTable = (TableLayout) findViewById(R.id.table_tablet);
            int tabletTableRowNum = tabletTable.getChildCount();
            tabletTable.removeViews(1, tabletTableRowNum - 1);
            try {
                // 盤タブレット設置状況の表示
                String[] tabletTableItem = {"名称", "状況"};  // タブレット設置状況の項目
                JSONObject responseJson = new JSONObject(recievedParam);
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
        }
    }
}
