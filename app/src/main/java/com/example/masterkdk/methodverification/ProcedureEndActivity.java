package com.example.masterkdk.methodverification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


/**
 * Created by masterkdk on 2016/11/11.
 * S-xx 手順書終了画面
 */

public class ProcedureEndActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedure_end);

        // ボタンへリスナを登録
        findViewById(R.id.ok_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.ok_button) {
            Intent intent = new Intent(this, EndActivity.class);
            startActivity(intent);
        }
    }
}
