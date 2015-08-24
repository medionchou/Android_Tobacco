package com.example.medionchou.tobacco;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LocalService extends Service implements Runnable {

    private final String SERVER_IP = "140.113.167.14";
    private final int SERVER_PORT = 9000;
    private final IBinder mBinder = new LocalBinder();

    private SocketChannel socketChannel;
    private Thread client;
    private ByteBuffer inputBuffer;
    private CharBuffer outStream;
    private String serverReply;
    private String cmd;
    private String queryReply;
    private String updateOnline;
    private List<Byte> buffer;

    private boolean isTerminated;
    private boolean isSignIn;
    private int client_state;

    @Override
    public void onCreate() {
        super.onCreate();
        initObject();
        client.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deRefObject();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        setUpConnection();
    }

    private void initObject() {
        isTerminated = false;
        isSignIn = false;
        client = new Thread(this);
        inputBuffer = ByteBuffer.allocate(2048);
        client_state = States.CONNECT_INITIALZING;
        serverReply = "";
        cmd = "";
        queryReply = "";
        updateOnline = "";
        buffer = new ArrayList<>();
        inputBuffer.clear();
    }

    private void deRefObject() {
        isTerminated = true;

    }

    private void setUpConnection() {

        try {
            while (!isTerminated) {

                if (socketChannel == null) {

                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);
                    socketChannel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

                    while (!socketChannel.finishConnect()) {
                        Thread.sleep(3000);
                    }

                } else if (socketChannel != null) {

                    int num;
                    while ((num = socketChannel.read(inputBuffer)) > 0) {
                        inputBuffer.flip();
                        while (inputBuffer.hasRemaining()) {
                            buffer.add(inputBuffer.get());
                        }
                        inputBuffer.clear();
                    }

                    if (num < 0)
                        throw new IOException("Server disconnect");

                    if (buffer.size() > 0) {
                        byte[] tmp = new byte[buffer.size()];
                        for (int i = 0; i < buffer.size(); i++)
                            tmp[i] = buffer.get(i);
                        String ttmp = new String(tmp, "UTF-8");
                        serverReply += ttmp;

                        Log.v("MyLog", ttmp);
                        buffer.clear();
                    }

                    while (serverReply.contains("<END>")) {

                        int endIndex = serverReply.indexOf("<END>") + 5;
                        if (endIndex < 0) endIndex = 0;

                        String endLine = serverReply.substring(0, endIndex);

                        //Log.v("MyLog", endLine);

                        if (endLine.contains("CONNECT_OK<END>")) {
                            client_state = States.CONNECT_OK;
                        } else if (endLine.contains("LOGIN_REPLY")) {
                            isSignIn = true;
                        } else if (endLine.contains("QUERY_REPLY")) {
                            queryReply = endLine;
                        } else if (endLine.contains("UPDATE_ONLINE")) {
                            updateOnline = endLine;
                        }

                        serverReply = serverReply.replace(endLine, "");
                    }


                    switch (client_state) {
                        case States.CONNECT_INITIALZING:
                            outStream = CharBuffer.wrap(Command.CONNECT_SERVER);
                            while (outStream.hasRemaining()) {
                                socketChannel.write(Charset.defaultCharset().encode(outStream));
                            }
                            Thread.sleep(2000);
                            outStream.clear();
                            break;
                        case States.CONNECT_OK:
                            if (cmd.length() > 0) {
                                outStream = CharBuffer.wrap(cmd);
                                while (outStream.hasRemaining()) {
                                    socketChannel.write(Charset.defaultCharset().encode(outStream));
                                }
                                cmd = "";
                                outStream.clear();
                            }
                            break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Log.e("MyLog", "InterruptedException " + e.toString());

        } catch (IOException e) {
            Log.e("MyLog", "IOException " + e.toString());
            stopSelf();
            startService(new Intent(this, LocalService.class));
            /* TODO:
                    Make sure reconnection will still work. At the same time, do not switch away from Bound Service.
             */
        } finally {
            try {
                if (socketChannel != null)
                    socketChannel.close();
            } catch (IOException err) {
                Log.v("MyLog", "IOException " + err.toString());
            }
        }
    }

    public int getClientState() {
        return client_state;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public boolean isLoggin() {
        return isSignIn;
    }

    public String getQueryReply() {
        return queryReply;
    }

    public String getUpdateOnline() {
        return updateOnline;
    }

    public void resetUpdateOnline() {
        updateOnline = "";
    }

    public class LocalBinder extends Binder {

        public LocalService getService() {
            return LocalService.this;
        }

    }

}
