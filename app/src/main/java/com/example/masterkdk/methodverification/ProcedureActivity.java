package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.masterkdk.methodverification.Helper.DataStructureHelper;
/*
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
*/
public class ProcedureActivity extends AppCompatActivity implements View.OnClickListener{

    // 通信
    private static final String HOST = "192.168.10.20";
        private static final int PORT = 1280;  // ポート(実環境)
//    private static final int PORT = 1234;  // ポート(VisualStudio)
    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final TransmissionFragment SEND_FRAGMENT = TransmissionFragment.newInstance(HOST,PORT);
    private static final DataStructureHelper DATA_STRUCTURE_HELPER = new DataStructureHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure);

        // ボタン(固定)へリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
        findViewById(R.id.site_difference_button).setOnClickListener(this);

        // 手順の表示処理
        final TextView noText = (TextView) findViewById(R.id.no_text);
        final TextView boardEquipmentText = (TextView) findViewById(R.id.board_equipment_text);
        final TextView instructText = (TextView) findViewById(R.id.instruct_text);
        TableLayout procedureTable = (TableLayout) findViewById(R.id.procedureTable);
//        String innerText = null;
        TableRow.LayoutParams columnLayout = new TableRow.LayoutParams();  // 通常の行のレイアウトパラメータ
        columnLayout.setMargins(8, 8, 8, 8);  // 動的なスタイル設定はstyles.xmlに書いたmarginを利用できない
        JSONObject rowJSONObject = null;
        String innerText = null;

        // Fragmentを利用した通信の準備
//        final TransmissionFragment sendFragment = TransmissionFragment.newInstance(HOST,PORT);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(SEND_FRAGMENT, TAG_TRANS);
        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行
//        final DataStructureHelper dataStructureHelper = new DataStructureHelper();
//        String data = null;

        // 手順スクロール部は設置状況確認画面で取得した値を利用する
        Intent pI = getIntent();
        String resultSt = pI.getStringExtra("resultStTmp");

        try {
            JSONObject responseJson = new JSONObject(resultSt);
            JSONArray procedureArray = responseJson.getJSONArray("tejun");

            // テーブルレイアウトに行を追加し、コメント又は通常の要素を加える
            for (int i = 0; i < procedureArray.length(); i++) {

                // 新テーブル行の生成
                final TableRow procedureRow = new TableRow(this);

                rowJSONObject = procedureArray.getJSONObject(i);

                // No.の挿入
                final TextView rowNoText = new TextView(this);
                innerText = rowJSONObject.getString("tx_sno");
                rowNoText.setText(innerText);
                procedureRow.addView(rowNoText, columnLayout);

//                if (procedureArray.getJSONObject(i).getString("tx_sno").equals("C")) {
                if (innerText.equals("C")) {
                    // Noが"C"の場合はコメントを追加
                    TextView addColumn = new TextView(this, null, R.attr.S04CommentRowTextDynamic);
                    innerText = rowJSONObject.getString("tx_com");
                    addColumn.setText(innerText);
                    procedureRow.addView(addColumn, columnLayout);
                } else {
                    // 通常の要素の追加

                    // 盤・機器名
                    final TextView boardEquipmentRowText = new TextView(this, null, R.attr.S04BoardEquipmentTextDynamic);
                    innerText = rowJSONObject.getString("tx_s_l");
                    boardEquipmentRowText.setText(innerText);
//                    boardEquipmentRowText.setText(procedureArray.getJSONObject(i).getString("tx_bname") + "\n" + procedureArray.getJSONObject(i).getString("tx_swname"));
                    procedureRow.addView(boardEquipmentRowText, columnLayout);

                    // 指示ボタン
                    final Button rowInstructButton = new Button(this, null, R.attr.S04InstructButtonDynamic);

//                    String tmp = rowJSONObject.getString("tx_clr2");  // 社長に値の変更依頼
             //       rowInstructButton.setBackgroundColor();  // 引数はint
/*
                    innerText = rowJSONObject.getString("tx_s_r" + "\n　");
                    rowInstructButton.setText(innerText);
*/
//                    rowInstructButton.setText(procedureArray.getJSONObject(i).getString("tx_action") + "\n　");
                    rowInstructButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                    rowInstructButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 指示ボタンクリック時の詳細処理

                            // ヘッダへの値表示(No、盤・機器名、指示名)
                            noText.setText(rowNoText.getText());
                            boardEquipmentText.setText(boardEquipmentRowText.getText());
                            instructText.setText(rowInstructButton.getText());

                            // コマンド送信 TODO:手順書番号はNo?
                            String data = DATA_STRUCTURE_HELPER.makeSendData("13","???");
                            SEND_FRAGMENT.send(data);

                            // 選択行の着色と指示ボタンの無効化
                            Resources res = getResources();
                            int lockColor = res.getColor(R.color.colorYellowButton);
                            procedureRow.setBackgroundColor(lockColor);
                            v.setEnabled(false);
                        }
                    });

                    innerText = rowJSONObject.getString("tx_s_r") + "\n　";
                    rowInstructButton.setText(innerText);

                     procedureRow.addView(rowInstructButton, columnLayout);
                }

                procedureTable.addView(procedureRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ボタン(固定)クリック時詳細処理
    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.site_difference_button) {  // 現場差異ボタン
            this.onClickSiteDiffButton(v);
        } else if (id == R.id.return_button) {    // MENUへ戻るボタン
            Intent intent = new Intent(this, TopActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        }
    }

    // ポップアップの初期化
    private PopupWindow mPopupWindow;

    public void onClickSiteDiffButton(View v) {

        mPopupWindow = new PopupWindow(ProcedureActivity.this);
//        static String data = null;

                // レイアウト設定
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // ボタン設定
        popupView.findViewById(R.id.procedure_skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順をスキップする
                String data = DATA_STRUCTURE_HELPER.makeSendData("14","?,1");
                SEND_FRAGMENT.send(data);
            }
        });
        popupView.findViewById(R.id.procedure_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順の前に操作を追加する
                String data = DATA_STRUCTURE_HELPER.makeSendData("14","?,2");
                SEND_FRAGMENT.send(data);
            }
        });
        popupView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow.isShowing()) {  // キャンセル
                    mPopupWindow.dismiss();
                }
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

        // ポップアップ削除時は、手順書画面フッタのメッセージも削除
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // キャンセルボタンだけでなく、ポップアップ外タップ時にも対応
                siteDifferenceText.setText("");
            }
        });
    }
}
