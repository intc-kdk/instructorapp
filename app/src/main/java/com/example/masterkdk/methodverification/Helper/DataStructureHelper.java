package com.example.masterkdk.methodverification.Helper;

import android.os.Bundle;
import android.util.AndroidRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 送受信データの
 */

public class DataStructureHelper {

    private static final String FORMAT = "^(\\d{2})@(.*)\\$$";
    private static final String JSON_FORMAT = "^\\{(.*)\\}$";
    private static final String DELIMITER = "@";
    private static final String TRAILER = "$";

    private String mRecievedData;
    private String mRecievedCmd;

    public DataStructureHelper(){

    }

    public String setRecievedData(String d){
        Pattern p = Pattern.compile(FORMAT);
        Matcher m = p.matcher(d);
        String cmd ="";

        if(m.find()){
            this.mRecievedCmd=m.group(1);
            this.mRecievedData=m.group(2);
            cmd = mRecievedCmd;
        }else{
            // TODO: 不正フォーマットの処理
            cmd = "99";
        }

        return cmd;
    }

    public Bundle getRecievedData(){

        Pattern p = Pattern.compile(JSON_FORMAT);
        Matcher m = p.matcher(mRecievedData);
        Bundle bdData = new Bundle();
        // 先頭、末尾のブラケットでのJSONフォーマットを簡易判定
        if(m.find()){
            //JSONデータ は"JSON" を返し、別メソッドでJSONObjectを取得
            final JSONObject jo;
            try {
                jo = new JSONObject(mRecievedData);
                bdData = this.toBundle(jo);   // JSONからBundleへ変換
            }
            catch (JSONException e){
                // 例外処理
                throw new AndroidRuntimeException(e);
            }
            bdData.putString("format","JSON");
        }else{
            //テキストデータ
            bdData.putString("format","TEXT");
            bdData.putString("text",mRecievedData);
        }
        bdData.putString("command",mRecievedCmd);
        return bdData;
    }
    public String getRecievedJson(){

        return "";
    }

    public String makeSendData(String cmd, String data){
        StringBuilder sb = new StringBuilder();

        sb.append(cmd);
        sb.append(DELIMITER);
        sb.append(data);
        sb.append(TRAILER);

        return sb.toString();
    }

    public static Bundle toBundle(final JSONObject json) {
        final Bundle bundle = new Bundle();
        final Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            if (json.isNull(key)) {
                bundle.putString(key, null);
                continue;
            }
            final Object value = json.opt(key);
            if (value instanceof JSONObject) {
                bundle.putBundle(key, toBundle((JSONObject) value));
            } else if (value instanceof JSONArray) {
                bundle.putParcelableArrayList(key, toBundle((JSONArray) value));
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (boolean) value);
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (int) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (long) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (double) value);
            }
        }
        return bundle;
    }

    /**
     * JSONArray を ArrayList<Bundle> に適切に変換して返す.
     *
     * @param array JSONArray
     * @return ArrayList<Bundle>
     */
    public static ArrayList<Bundle> toBundle(final JSONArray array) {
        final ArrayList<Bundle> bundles = new ArrayList<>();
        for (int i = 0, size = array.length(); i < size; i++) {
            bundles.add(toBundle(array.optJSONObject(i)));
        }
        return bundles;
    }
}
