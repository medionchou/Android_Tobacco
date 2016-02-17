package com.example.medionchou.tobacco;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Medion-PC on 2016/2/3.
 */
public class Log {

    private static String site = "http://140.113.167.14/wlog.php?";
    private static String ID = "CT";

    public static synchronized void getRequest(String words) {

        words = words.replaceAll("<END>", "");
        words = words.replaceAll("<N>", "&nbsp;");

        final String msg = words;

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (Log.class) {
                    try {
                        String mySite = site + "ID=" + ID + "&Log=" + URLEncoder.encode(msg + "\n", "UTF-8");
                        URL url = new URL(mySite);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        rd.close();
                    } catch (Exception e) {
                        android.util.Log.e("MyLog", "Unable write log ! ");
                    }
                }
            }
        }).start();

    }
}
