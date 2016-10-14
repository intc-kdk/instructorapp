package com.example.masterkdk.methodverification;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.masterkdk.methodverification.Helper.SettingPrefUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReceptionFragment.ReceptionFragmentListener} interface
 * to handle interaction events.
 * Use the {@link ReceptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceptionFragment extends Fragment {
    private static final String ARG_PORT = "port";

    private ServerSocket mServer = null;
    private Socket mSocket = null;
    private int mPort;

    private ReceptionFragmentListener mListener;

    public ReceptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReceptionFragment.
     */
    public static ReceptionFragment newInstance() {
        ReceptionFragment fragment = new ReceptionFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ShraedPreferencesから取得
        Context context = getActivity();
        mPort = SettingPrefUtil.getClientPort(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());

        return textView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ReceptionFragmentListener) {
            mListener = (ReceptionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /* AsyncTask */
    public void listen(){
        new AsyncTask<Void,StringBuilder,String>(){
            BufferedReader reader = null;
            BufferedWriter writer = null;
            String response = "";
            @Override
            protected void onPreExecute(){

            }
            @Override
            protected String doInBackground(Void... voids) {
                String message = "";
                try{
                    mServer = new ServerSocket(mPort);
                    mServer.setReuseAddress(true);
                    mSocket = mServer.accept();
                    reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

                    //リクエスト受信
                    int result;
                    StringBuilder builder = new StringBuilder();
                    while((result = reader.read()) != -1 ){
                        builder.append((char)result);
                        if(result == 36) {
                            break;
                        }
                    }
                    message=builder.toString();

                    // Activity へ リクエストを返し、返信データ（response）を受け取る
                    response = ((ReceptionFragmentListener)getActivity()).onRequestRecieved(message);

                    if(response.equals("")) {
                        // データ未設定の時、コネクションクローズ
                        reader.close();
                        mSocket.close(); //
                    }else{
                        // データが設定されているとき、レスポンス送信
                        writer.write(response);
                        writer.flush();
                        reader.close();
                        mSocket.close(); //
                    }

                }catch (SocketException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                } finally {
                    /*try{
                        reader.close();
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
                // publishProgress();  // onProgressUpdateが呼ばれる
                return message;
            }
            @Override
            protected void onProgressUpdate(StringBuilder... message){
                String result = message.toString();
                System.out.println(message.toString());
            }
            //doInBackGroundの結果を受け取る
            @Override
            protected void onPostExecute(String result){
                //mTextView.setText(result);
            }
        }.execute();
    }

    /**

     */
    public interface ReceptionFragmentListener {
        String onRequestRecieved(String data);
    }
}
