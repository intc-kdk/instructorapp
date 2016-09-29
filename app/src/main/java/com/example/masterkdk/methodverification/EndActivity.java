package com.example.masterkdk.methodverification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by masterkdk on 2016/09/26.
 * S-06 終了方法選択画面
 */

public class EndActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // ボタンへリスナを登録
        findViewById(R.id.return_button).setOnClickListener(this);
    }

    // ボタンクリック時詳細処理
    @Override
    public void onClick(View v){
        int id = v.getId();
        Intent intent = null;
        if (id == R.id.return_button) {
            intent = new Intent(this, TopActivity.class);
            startActivity(intent);
        }
    }
}
