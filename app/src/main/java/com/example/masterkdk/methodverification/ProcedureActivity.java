package com.example.masterkdk.methodverification;

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

public class ProcedureActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure);

        // ボタンへリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
        findViewById(R.id.site_difference_button).setOnClickListener(this);

        // 手順の表示処理
        final TextView noText = (TextView) findViewById(R.id.no_text);
        final TextView boardEquipmentText = (TextView) findViewById(R.id.board_equipment_text);
        final TextView instructText = (TextView) findViewById(R.id.instruct_text);
        TableLayout procedureTable = (TableLayout) findViewById(R.id.procedureTable);
        String innerText = null;
        TableRow.LayoutParams columnLayout = new TableRow.LayoutParams();  // 通常の行のレイアウトパラメータ
        columnLayout.setMargins(8, 8, 8, 8);  // 動的なスタイル設定はstyles.xmlに書いたmarginを利用できない

        Intent pI = getIntent();
        String resultSt = pI.getStringExtra("resultStTmp");

        try {
            JSONObject responseJson = new JSONObject(resultSt);
            JSONArray procedureArray = responseJson.getJSONArray("tejun");

            for (int i = 0; i < procedureArray.length(); i++) {

                final TableRow procedureRow = new TableRow(this);

                final TextView rowNoText = new TextView(this);
                rowNoText.setText(Integer.toString(i + 1));
                procedureRow.addView(rowNoText, columnLayout);

                if (procedureArray.getJSONObject(i).getString("tx_sno").equals("C")) {
                    // Noが"C"の場合はコメント行を追加
                    TextView addColumn = new TextView(this, null, R.attr.S04CommentRowTextDynamic);
                    innerText = procedureArray.getJSONObject(i).getString("tx_com");
                    addColumn.setText(innerText);
                    procedureRow.addView(addColumn, columnLayout);
                } else {
                    final TextView boardEquipmentRowText = new TextView(this, null, R.attr.S04BoardEquipmentTextDynamic);
                    boardEquipmentRowText.setText(procedureArray.getJSONObject(i).getString("tx_bname") + "\n" + procedureArray.getJSONObject(i).getString("tx_swname"));
                    procedureRow.addView(boardEquipmentRowText, columnLayout);

                    final Button rowInstructButton = new Button(this, null, R.attr.S04InstructButtonDynamic);
                    rowInstructButton.setText(procedureArray.getJSONObject(i).getString("tx_action") + "\n　");
                    rowInstructButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                    rowInstructButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 指示ボタンクリック時の詳細処理

                            noText.setText(rowNoText.getText());
                            boardEquipmentText.setText(boardEquipmentRowText.getText());
                            instructText.setText(rowInstructButton.getText());

                            Resources res = getResources();
                            int lockColor = res.getColor(R.color.colorYellowButton);
                            procedureRow.setBackgroundColor(lockColor);

                            v.setEnabled(false);
                        }
                    });

                     procedureRow.addView(rowInstructButton, columnLayout);
                }

                procedureTable.addView(procedureRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ボタンクリック時詳細処理
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

    // ポップアップ関連処理
    private PopupWindow mPopupWindow;

    public void onClickSiteDiffButton(View v) {

        mPopupWindow = new PopupWindow(ProcedureActivity.this);

        // レイアウト設定
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // ボタン設定
        popupView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow.isShowing()) {  // キャンセルボタン
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
