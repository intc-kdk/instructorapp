package com.example.masterkdk.methodverification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


/**
 * Created by masterkdk on 2016/09/23.
 * S-03 確認画面
 */

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // ボタンへリスナを登録
        findViewById(R.id.ok_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.ok_button) {
            Intent intent = new Intent(this, ProcedureActivity.class);
            startActivity(intent);
        }
    }
}
