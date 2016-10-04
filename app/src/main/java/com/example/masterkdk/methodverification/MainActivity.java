package com.example.masterkdk.methodverification;

import android.content.Intent;
//import android.graphics.Paint;
import android.content.Loader;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
//import android.widget.Button;
//import android.app.Activity;

import android.view.ViewGroup;

import android.app.LoaderManager;
import com.example.masterkdk.methodverification.loader.SendRequestLoader;

import java.nio.charset.StandardCharsets;
//import com.example.masterkdk.methodverification.net.TcpClient;


//public class MainActivity extends AppCompatActivity implements OnClickListener{
public class MainActivity extends AppCompatActivity implements OnClickListener, LoaderManager.LoaderCallbacks<String> {

    /*
     * サーバとの通信処理(作業の為、一時的に先頭に置く)
     */

    private TextView mTextView;
    private static final String HOST = "192.168.10.20";  // 実環境
//    private static final int PORT = 1280;  // 実環境
    private static final int PORT = 1234; // v003の開発用サーバ
//    private TcpClient client;
//    private static final String DATA = "data";

    public void startAsyncLoad(String data){
//    public String startAsyncLoad(String data){
        Bundle args = new Bundle();
        args.putString("Data",data);
        // Loaderを初期化する
        getLoaderManager().initLoader(0, args, this);  // onCreateLoaderが呼ばれる
//        SendRequestLoader lstr = (SendRequestLoader) getLoaderManager().initLoader(0, args, this);  // onCreateLoaderが呼ばれる
//        String str = lstr.loadInBackground();
//        return str;
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args){

        if(args != null) {
            String data = args.getString("Data");
            SendRequestLoader srl = new SendRequestLoader(this, HOST, PORT, data);
            return  new SendRequestLoader(this, HOST, PORT, data);
        } else {
            return null;
        }
/*        if(args != null) {
            String data = args.getString("Data");
            return  new SendRequestLoader(this, HOST, PORT, data);
        }
        return null;*/
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String result) {
        int id = loader.getId();
        mTextView.setText(result);
        getLoaderManager().destroyLoader(id);  // ローダーを破棄
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        String st = "aaa";
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // サーバへ指示者アプリの活性を通知
        // コマンドはアスキー文字列で変換の必要あり
        String str1 = "10@";
        byte[] bytes = str1.getBytes(StandardCharsets.UTF_8);
        String str = new String(bytes, StandardCharsets.US_ASCII);
        startAsyncLoad(str);
//        startAsyncLoad("10@");  // VB側で、受信ログ出せたけどエラーも出た
//        String str = startAsyncLoad("10");  // TCPシミュレータには"10"送れた


        // ボタンへリスナを登録
        findViewById(R.id.menu_button).setOnClickListener(this);
        findViewById(R.id.start_button).setOnClickListener(this);

        // TableLayout実装用の仮配列
        String[] tabletTableRow1 = {"Javaで出力1", "Java"};
        String[] procedureTableRow = {"Javaで出力1", "Javaで出力2", "Javaで出力3", "Javaで出力4", "Javaで出力5", "Javaで出力6", "Javaで出力7", "Javaで出力8"};

        // 盤タブレット設置状況の表示  TODO:盤タブレット数だけテーブルに行追加
        TableLayout tableTablet = (TableLayout) findViewById(R.id.table_tablet);
        TableRow addTableRow = null;
        TextView addTextView1 = null;
        TextView addTextView2 = null;

        addTableRow = new TableRow(this);
//        addTableRow.setPadding(2, 2, 2, 2);  // 行が膨らむが、外枠は表示

//        addTextView1 = new TextView(this);
        addTextView1 = new TextView(this, null, R.attr.testStyle);
/*
        // java.lang.NullPointerException: Attempt to invoke virtual method 'void android.view.ViewGroup$MarginLayoutParams.setMargins(int, int, int, int)' on a null object
        LayoutParams lp = addTextView1.getLayoutParams();
        MarginLayoutParams mlp = (MarginLayoutParams) lp;
        mlp.setMargins(2, 2, 2, 2);
*/
//        addTextView1.setPadding(4, 4, 4, 4);  // marginがだめなら行にpaddingはどうだ
        addTextView1.setText(tabletTableRow1[0]);
        addTableRow.addView(addTextView1);

        addTextView2 = new TextView(this);
//        addTextView2 = new TextView(this, "style", R.style.S01TableColumnTablet);
        addTextView2 = new TextView(this, null, R.attr.testStyle);
//        addTextView2 = new TextView(this, null, R.attr.S01TableColumnTabletDynamic);
//        addTextView2 = new TextView(this, null, R.style.S01TableColumnTablet);
//        addTextView2 = new TextView(this, null, R.id.S01TableColumnTablet);
//        addTextView2.setPadding(4, 4, 4, 4);  // 上と下に膨らむ
        addTextView2.setText(tabletTableRow1[1]);
        addTableRow.addView(addTextView2);

        tableTablet.addView(addTableRow);
//        tableTablet.set

        // 手順一覧の表示  TODO: 手順数だけテーブルに行追加
        TableLayout tableProcedure = (TableLayout) findViewById(R.id.table_procedure);

//        addTableRow = new TableRow(this);

        for(int h = 0; h < 100; h++) {  // 100は検証用の数
            addTableRow = new TableRow(this);

            for (int i = 0; i < 8; i++) {
//                addTextView1 = new TextView(this);
                addTextView1 = new TextView(this, null, R.attr.testStyle);
/*
                Resources res = getResources();
                int rowStyle = res.get();
                //linearLayoutRow.setBackgroundColor(lockColor);
*/
                addTextView1.setText(procedureTableRow[i]);

                addTableRow.addView(addTextView1);
            }

            tableProcedure.addView(addTableRow);
        }

//        tableProcedure.addView(addTableRow);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.menu_button) {
            Intent intent = new Intent(this, TopActivity.class);
            startActivity(intent);
        } else if (id == R.id.start_button) {
            Intent intent = new Intent(this, ProcedureActivity.class);
            startActivity(intent);
        }
    }
}
