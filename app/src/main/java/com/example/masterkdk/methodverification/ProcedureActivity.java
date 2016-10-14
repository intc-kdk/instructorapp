package com.example.masterkdk.methodverification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
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


//public class ProcedureActivity extends AppCompatActivity implements View.OnClickListener{
//public class ProcedureActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener {
public class ProcedureActivity extends FragmentActivity implements View.OnClickListener, TransmissionFragment.TransmissionFragmentListener, ReceptionFragment.ReceptionFragmentListener {
    // 通信
    private static final String HOST = "192.168.10.20";
//    private static final int PORT = 1280;  // ポート(実環境)
    private static final int PORT = 1234;  // ポート(VisualStudio)
    private static final String TAG_TRANS = "No_UI_Fragment1";
    private static final String TAG_RECEP = "No_UI_Fragment2";
    private static final TransmissionFragment SEND_FRAGMENT = TransmissionFragment.newInstance(HOST,PORT);

    private static final ReceptionFragment recieveFragment = ReceptionFragment.newInstance();

    private static final DataStructureHelper DATA_STRUCTURE_HELPER = new DataStructureHelper();
    private TableRow instructProcedure = null;  // 指示後の行
    private String instructNo = "";  // 指示後の手順書番号
    private TableLayout instructProcedureTable = null;

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
        TableRow.LayoutParams columnLayout = new TableRow.LayoutParams();  // 通常の行のレイアウトパラメータ
        columnLayout.setMargins(8, 8, 8, 8);  // 動的なスタイル設定はstyles.xmlに書いたmarginを利用できない
        JSONObject rowJSONObject = null;  // 1行毎のJSONObject
        String innerText = null;  // 表示文字列の一時的な保管
        String beforeRowStatus = "0";  // 1段上の手順の状態(cd_status)
        boolean beforeRowDifference = false;
        String stringTemp = null;
        int[] intArrayTemp = {};
        Boolean booleanTemp = false;
        Boolean booleanTemp2 = false;

        // 通信用Fragment初期化
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(SEND_FRAGMENT, TAG_TRANS);

        transaction.add(recieveFragment, TAG_RECEP);

        transaction.commit();
        fragmentManager.executePendingTransactions();   // 即時実行

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

                // Noを表示
                final TextView rowNoText = new TextView(this);
                innerText = rowJSONObject.getString("tx_sno");
                rowNoText.setText(innerText);
                procedureRow.addView(rowNoText, columnLayout);

                if (innerText.equals("C")) {
                    // Noが"C"の場合はコメントを追加
                    rowNoText.setVisibility(View.INVISIBLE);  // コメント行はNo非表示
                    TextView addColumn = new TextView(this, null, R.attr.S04CommentRowTextDynamic);
                    innerText = rowJSONObject.getString("tx_com");
                    addColumn.setText(innerText);
                    procedureRow.addView(addColumn, columnLayout);
                } else {
                    // 通常の行の追加

                    // 盤・機器名
                    final TextView boardEquipmentRowText = new TextView(this, null, R.attr.S04BoardEquipmentTextDynamic);
                    innerText = rowJSONObject.getString("tx_s_l");
                    boardEquipmentRowText.setText(innerText);
                    procedureRow.addView(boardEquipmentRowText, columnLayout);

                    // 指示ボタン
                    final Button rowInstructButton = new Button(this, null, R.attr.S04InstructButtonDynamic);

//                    stringTemp = "#" + rowJSONObject.getString("tx_clr2");
//                    rowInstructButton.setBackgroundColor(Color.parseColor(stringTemp));

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

                            // コマンド送信
                            //hashCode()
                            String data = DATA_STRUCTURE_HELPER.makeSendData("13","{\"手順書番号\":\"" + rowNoText.getText() + "\"}");
                            SEND_FRAGMENT.send(data);
/*
                            // 選択行の着色と指示ボタンの無効化
                            Resources res = getResources();
                            int lockColor = res.getColor(R.color.colorYellowButton);
                            procedureRow.setBackgroundColor(lockColor);
                            v.setEnabled(false);
*/
                            // 後のイベント処理の為に値を控えておく
                            instructProcedure = procedureRow;
                            instructNo = rowNoText.getText().toString();
                        }
                    });

                    innerText = rowJSONObject.getString("tx_s_r") + "\n　";
                    rowInstructButton.setText(innerText);

                    // 行の状態を設定
                    stringTemp = rowJSONObject.getString("cd_status");
                    booleanTemp = rowJSONObject.getBoolean("bo_gs");
                    if (stringTemp.equals("1") && !booleanTemp) {  // 指示後かつ現場差異無しの行を着色
                        procedureRow.setBackgroundColor(getResources().getColor(R.color.colorYellowButton));
                        instructProcedure = procedureRow;
                        instructNo = stringTemp;
                    }
                    // ボタンの状態を設定
                    if((i == 0 && stringTemp.equals("0"))  // 指示できる順番の未発令の手順
                            || (stringTemp.equals("0") && beforeRowStatus.equals("7"))
                            )
//                            || (stringTemp.equals("0") && beforeRowDifference))  // TODO:現場差異は後回し
                    {
//                    if(i == 0 && stringTemp.equals("0")) {
                        booleanTemp2 = true;
                    } else if(!stringTemp.equals("0")) {
                        booleanTemp2 = false;  // 指示後及び確認後の手順
                        stringTemp = "#" + rowJSONObject.getString("tx_clr2");
                        rowInstructButton.setBackgroundColor(Color.parseColor(stringTemp));
                        rowInstructButton.setTextColor(Color.parseColor("#FFFFFF"));
                    } else {
                        booleanTemp2 = false;  // まだ指示できない未発令の手順
                    }
                    rowInstructButton.setEnabled(booleanTemp2);
                    beforeRowStatus = stringTemp;
                    beforeRowDifference = booleanTemp;

                     procedureRow.addView(rowInstructButton, columnLayout);
                }

                procedureTable.addView(procedureRow);

                instructProcedureTable = procedureTable;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // サーバからの応答受信
    @Override
    public void onResponseRecieved(String data) {

        System.out.println("ResRecieved");

        DataStructureHelper dsHelper = new DataStructureHelper();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド
        System.out.println("Command：" + cmd);

        if (cmd.equals("50")) {  // 発令が受信された

            // 選択行の着色と指示ボタンの無効化
            int lockColor = getResources().getColor(R.color.colorYellowButton);
            instructProcedure.setBackgroundColor(lockColor);
            instructProcedure.getChildAt(2).setEnabled(false);

//        if (cmd.equals("55")) {  // 確認者アプリで確認が行われた
/*
        } else if (cmd.equals("55")) {  // 確認者アプリで確認が行われた
            // 指示中の手順を確認済みへ変更　行の着色を消し、指示ボタンの色をtx_cle2の値、下の手順の指示ボタンを利用可能にする
            int instructNoInt = Integer.parseInt(instructNo);
            TableRow instructRow = (TableRow) instructProcedureTable.getChildAt(instructNoInt);
            instructRow.setBackgroundColor(Color.parseColor("FF000000"));
            TableRow instructRowNew = (TableRow) instructProcedureTable.getChildAt(instructNoInt + 1);
            instructRowNew.getVirtualChildAt(2).setEnabled(true);
*/

        } else if (cmd.equals("56")) {
            // 指示前又は指示中の手順を現場差異の表示　行を着色、下の手順の指示ボタンを利用可能にする

//            int elementNum = instructProcedure.getVirtualChildCount();
            int elementNum = instructProcedure.getChildCount();
            int diffColor = getResources().getColor(R.color.colorDiffElement);
            for (int i = 0; i < elementNum; i++) {
                instructProcedure.getChildAt(i).setBackgroundColor(diffColor);
//                instructProcedure.getVirtualChildAt(i).setBackgroundColor(diffColor);
            }
//            View v = instructProcedure.getVirtualChildAt(1);
//            System.out.println("出力：" + v);

            mPopupWindow.dismiss();
        }
    }

    @Override
    public void onFinishTransmission(String data){
        // 実装する処理はないが、インターフェイス利用の為にオーバーライドが必要
    }

    /* 要求受信 */
    @Override
    public String onRequestRecieved(String data) {
        // サーバーからの要求（data）を受信
        //System.out.println("ReqRecieved:"+data);
        DataStructureHelper dsHelper = new DataStructureHelper();

        String cmd = dsHelper.setRecievedData(data);  // データ構造のヘルパー 受信データを渡す。戻り値はコマンド

        if (cmd.equals("55")) {  // 確認者アプリで確認が行われた
            // 指示中の手順を確認済みへ変更　行の着色を消し、指示ボタンの色をtx_cle2の値、下の手順の指示ボタンを利用可能にする
            int instructNoInt = Integer.parseInt(instructNo);
            TableRow instructRow = (TableRow) instructProcedureTable.getChildAt(instructNoInt);
            instructRow.setBackgroundColor(Color.parseColor("FF000000"));
            TableRow instructRowNew = (TableRow) instructProcedureTable.getChildAt(instructNoInt + 1);
            instructRowNew.getVirtualChildAt(2).setEnabled(true);
        }

        return cmd;
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

        // ボタン設定  TODO:現場差異は指示の有無に関わらず可能にする
        popupView.findViewById(R.id.procedure_skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順をスキップする
                String data = DATA_STRUCTURE_HELPER.makeSendData("14","{\"手順書番号\":\"" + instructNo + "\",\"コマンド\":\"1\"}");
                SEND_FRAGMENT.send(data);
            }
        });
        popupView.findViewById(R.id.procedure_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // この手順の前に操作を追加する  TODO:検証未実施
                String data = DATA_STRUCTURE_HELPER.makeSendData("14","{\"手順書番号\":\"\" + instructNo + \",\"コマンド\":\"2\"}");
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
