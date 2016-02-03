package com.example.medionchou.tobacco;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Medion-PC on 2016/2/3.
 */
public class Log {

    private static String site = "http://140.113.167.14/wlog.php?";

    public static synchronized void getRequest(String words) throws Exception {

        URL url = new URL(site + "ID=CT&Log=" + words);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
    }

}
