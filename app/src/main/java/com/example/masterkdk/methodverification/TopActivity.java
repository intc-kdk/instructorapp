package com.example.masterkdk.methodverification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by masterkdk on 2016/09/21.
 * S-02 トップページ
 */

public class TopActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        // ボタンへリスナを登録
        findViewById(R.id.start_button).setOnClickListener(this);
        findViewById(R.id.end_button).setOnClickListener(this);
        findViewById(R.id.confirm_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){

        int id = v.getId();
        Intent intent = null;

        if (id == R.id.start_button) {
            intent = new Intent(this, ConfirmActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        } else if (id == R.id.end_button) {
            intent = new Intent(this, EndActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        } else if (id == R.id.confirm_button) {
            intent = new Intent(this, MainActivity.class);

            Intent pI = getIntent();
            intent.putExtra("resultStTmp", pI.getStringExtra("resultStTmp"));

            startActivity(intent);
        }
    }
}
