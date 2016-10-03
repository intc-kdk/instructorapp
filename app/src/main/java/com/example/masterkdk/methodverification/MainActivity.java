package com.example.masterkdk.methodverification;

import android.content.Intent;
//import android.graphics.Paint;
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

public class MainActivity extends AppCompatActivity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
