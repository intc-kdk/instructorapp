package com.example.masterkdk.methodverification.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by takashi on 2016/09/27.
 */

public class TcpClient {
    private String mSendData;
    private String mHost;
    private int mPort;

    public TcpClient(String host, int port, String data)
    {
        this.mHost = host;
        this.mPort = port;
        this.mSendData = data;
    }

    public String connect() {
        Socket connection = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String message = "result:";
        try {
            //ソケット
            connection = new Socket(mHost, mPort);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            //リクエスト
            writer.write(mSendData);
            writer.flush();

            //HTTPレスポンス
            String result;
            while((result = reader.readLine()) != null) {
                message += result;
                message += "\n";
            }

        } catch (IOException e) {
            message = "IOException error: " + e.getMessage();
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            message = "Exception: " + e.getMessage();

        } finally {
            try{
                reader.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(message);
        return message;
    }
}
