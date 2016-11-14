package com.example.masterkdk.methodverification.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.masterkdk.methodverification.R;

/**
 * Created by takashi on 2016/11/13.
 */

public class alertDialog {

    private alertDialog(){}

    public static void show(Activity activity, String title, String msg){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(R.drawable.error)
                .setPositiveButton( "OK",
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
    }
}
