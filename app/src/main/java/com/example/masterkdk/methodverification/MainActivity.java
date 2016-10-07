package com.example.masterkdk.methodverification;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.LoaderManager;
import com.example.masterkdk.methodverification.loader.SendRequestLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity implements OnClickListener, LoaderManager.LoaderCallbacks<String> {

    /*
     * サーバとの通信処理
     */
    private static final String HOST = "192.168.10.20";  // 実環境ホスト
//    private static final int PORT = 1280;  // 実環境ポート
    private static final int PORT = 1234; // 開発環境ポート
    private String[] serverResponse = null;

    public void startAsyncLoad(String data){
        Bundle args = new Bundle();
        args.putString("Data",data);
        // Loaderを初期化する
        getLoaderManager().initLoader(0, args, this);  // onCreateLoaderが呼ばれる
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args){
        if(args != null) {
            String data = args.getString("Data");
            return  new SendRequestLoader(this, HOST, PORT, data);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String result) {

        try {
            // 盤タブレット設置状況の表示
            String[] tabletTableItem = {"名称", "状況"};  // タブレット設置状況の項目
            String[] resultArr = result.split("@");  // 受信値:コマンド@表示値(JSON)
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
                    if (tabletTableItem[j].equals("状況")) {
                        innerText = (innerText.equals("False")) ? "" : "○";  // 状況の表示値を変換
                        innerText = (innerText.equals("")) ? "" : "";  // 状況の表示値を変換
                    }
                    addColumn.setText(innerText);
                    addRow.addView(addColumn, columnLayout);
                }
                tableTablet.addView(addRow);
            }

            // 手順一覧の表示
            JSONArray procedureArray = responseJson.getJSONArray("tejun");
            TableLayout tableProcedure = (TableLayout) findViewById(R.id.table_procedure);
            String[] procedureTableItem = {"No", "場所", "盤", "機器", "指示", "備考", "時刻", "差異"};  // 手順一覧の項目(コメント除く)
            columnNum = procedureTableItem.length;  // 手順一覧の列数
            // コメント行のレイアウトパラメータ
            TableRow.LayoutParams commentColumnLayout = new TableRow.LayoutParams();
            commentColumnLayout.setMargins(2, 2, 2, 2);
            commentColumnLayout.span = columnNum;
            String tableItem = null;
            // 行の追加
            for (int i = 0; i < procedureArray.length(); i++) {
                addRow = new TableRow(this);
                // 列を左から追加
                for (int j = 0; j < columnNum; j++) {
                    tableItem = procedureTableItem[j];
                    innerText = procedureArray.getJSONObject(i).getString(tableItem);
                    if (tableItem.equals("No") && innerText.equals("C")) {
                        // Noが"C"の場合はコメント行を追加
                        innerText = procedureArray.getJSONObject(i).getString("コメント");
                        addColumn = new TextView(this, null, R.attr.S01ProcedureTableCommentColumnDynamic);
                        addColumn.setText(innerText);
                        addRow.addView(addColumn, commentColumnLayout);
                        break;
                    } else {
                        // 通常の行の追加
                        addColumn = new TextView(this, null, R.attr.S01ProcedureTableColumnDynamic);
                        addColumn.setText(innerText);
                        addRow.addView(addColumn, columnLayout);
                    }
                }
                tableProcedure.addView(addRow);
            }

            // 手順書の保存
            this.resultStTmp = resultArr[1];
/*
            Context apc = getApplicationContext();
            SharedPreferences sp = apc.getSharedPreferences("tejyunSp", Context.MODE_PRIVATE);
            SharedPreferences sp = this.getSharedPreferences("tejyunSp", Context.MODE_PRIVATE);
            SharedPreferences sp = getSharedPreferences("tejyunSp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("tejyun", resultArr[1]);
            editor.apply();
*/
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int id = loader.getId();
        getLoaderManager().destroyLoader(id);  // ローダーを破棄
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        //String st = "aaa";
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // サーバへ指示者アプリの活性(コマンド:10)を送信
        String str1 = "10@$";
        // コマンドを含むリクエスト文字列はASCIIでエンコード
        byte[] bytes = str1.getBytes(StandardCharsets.UTF_8);
        String str = new String(bytes, StandardCharsets.US_ASCII);
        startAsyncLoad(str);
/*        while (serverResponse == null) {
            TODO:テーブル表示と同期させるため処理を一旦止める
            // この方法では、ここで処理が止まってしまい、SendRequestLoaderの処理を開始できない
        }
*/

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
}
