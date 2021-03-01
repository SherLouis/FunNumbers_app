package com.llightning.funnumbers;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHandler {

    @Nullable
    public static String textFromUrl(String url){
        Log.d("HttpHandler", "Getting from url ...");
        String result = null;
        try{
            URL urls = new URL(url);
            Log.d("HttpHangler", "Opening connection to url ...");
            HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
            conn.setReadTimeout(3000); // 3 sec timeout
            conn.setConnectTimeout(3000); // 3 sec timeout
            conn.setRequestMethod("GET");
            Log.d("HttpHandler", "Trying to connect");
            conn.connect();
            Log.d("HttpHandler", "Connected");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                Log.d("HttpHandler", "Response 200 Ok");
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                Log.d("HttpHandler", "Building response string");
                while((line = reader.readLine()) != null){
                    stringBuilder.append(line).append("\n");
                }
                result = stringBuilder.toString();
            }
            conn.disconnect();
        } catch (IOException e) {
            Log.e("HttpHandler", e.toString());
        }
        Log.d("HttpHandled", "Response for request at url " + url + " : " + result);
        return result;
    }

}
