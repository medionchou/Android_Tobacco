package com.example.medionchou.tobacco;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.util.Log;

import com.example.medionchou.tobacco.Activity.MainActivity;
import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.Constants.States;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LocalService extends Service implements Runnable {

    private String SERVER_IP = "140.113.167.14";
    private int SERVER_PORT = 9000;
    private final IBinder mBinder = new LocalBinder();

    private SocketChannel socketChannel;
    private Thread client;
    private ByteBuffer inputBuffer;
    private CharBuffer outStream;
    private String serverReply;
    private String cmd;
    private String queryReply;
    private String updateOnline;
    private String updateMsg;
    private String updateQual;
    private String msg;
    private String swapDoneMsg;
    private String recentBox;
    private String exeResult;
    private List<Byte> buffer;

    private boolean isTerminated;
    private boolean isSignIn;
    private int client_state;

    @Override
    public void onCreate() {
        super.onCreate();
        /*initObject();
        client.start();*/
        //Log.v("MyLog", "ServiceOnCreate");
        SharedPreferences settings = getSharedPreferences(Config.IPCONFIG, 0);
        SERVER_IP = settings.getString("IP", "140.113.167.14");
        SERVER_PORT = settings.getInt("PORT", 9000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initObject();
        client.start();
        Log.v("MyLog", "ServiceStart");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //Log.v("MyLog", "ServiceBound");
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
        updateMsg = "";
        msg = "";
        swapDoneMsg = "";
        recentBox = "";
        exeResult = "";
        updateQual = "";
        buffer = new ArrayList<>();
        inputBuffer.clear();
        socketChannel = null;
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
                        Thread.sleep(2000);
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

                        if (buffer.get(buffer.size() - 1) > 0) {
                            byte[] tmp = new byte[buffer.size()];
                            for (int i = 0; i < tmp.length; i++)
                                tmp[i] = buffer.get(i);
                            serverReply += new String(tmp, "UTF-8");
                            buffer.clear();
                        }
                    }

                    while (serverReply.contains("<END>")) {

                        int endIndex = serverReply.indexOf("<END>") + 5;
                        if (endIndex < 0) endIndex = 0;

                        String endLine = serverReply.substring(0, endIndex);

                        Log.v("MyLog", endLine);

                        if (endLine.contains("CONNECT_OK<END>")) {
                            client_state = States.CONNECT_OK;
                        } else if (endLine.contains("LOGIN_REPLY")) {
                            isSignIn = true;
                        } else if (endLine.contains("QUERY_REPLY")) {
                            queryReply = endLine;
                        } else if (endLine.contains("UPDATE")) {
                            if (endLine.contains("UPDATE_ONLINE")) {
                                updateOnline = endLine;
                            } else if (!endLine.contains("UPDATE_VALUE")){
                                synchronized (updateMsg) {
                                    updateMsg += endLine;
                                }
                            } else if (endLine.contains("UPDATE_VALUE")) {
                                synchronized (updateQual) {
                                    updateQual += endLine;
                                }
                            }

                        } else if (endLine.contains("MSG")) {
                            String tmp;
                            tmp = endLine.replace("<END>", "");
                            tmp = tmp.replace("MSG\t", "");
                            msg = tmp;
                        } else if (endLine.contains("SWAP_DONE")) {
                            swapDoneMsg = endLine;
                        } else if (endLine.contains("BOX_RECENT")) {
                            recentBox = endLine;
                        } else if (endLine.contains("EXE")) {
                            exeResult = endLine;
                        }

                        serverReply = serverReply.replace(endLine, "");
                    }


                    switch (client_state) {
                        case States.CONNECT_INITIALZING:
                            outStream = CharBuffer.wrap(Command.CONNECT_SERVER);
                            while (outStream.hasRemaining() && socketChannel != null) {
                                socketChannel.write(Charset.defaultCharset().encode(outStream));
                            }
                            Thread.sleep(2000);
                            outStream.clear();
                            break;
                        case States.CONNECT_OK:
                            if (cmd.length() > 0) {
                                outStream = CharBuffer.wrap(cmd);
                                Log.v("MyLog", cmd);
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
            if (e.toString().contains("Server disconnect") || e.toString().contains("SocketTimeoutException") || e.toString().contains("ECONNRESET") ) {
                stopSelf();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                /* TODO:
                        Make sure reconnection will still work. At the same time, do not switch away from Bound Service.
                 */
                Log.v("MyLog", "Re-connection");
            }

        } catch(NotYetConnectedException e) {

            Log.e("MyLog", e.toString());
            stopSelf();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        } finally {
            try {
                if (socketChannel != null)
                    socketChannel.close();
                isTerminated = true;
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

    public String getUpdateOnlineMsg() {
        return updateOnline;
    }

    public String getMsg() {
        return msg;
    }

    public String getSwapDoneMsg() {
        return swapDoneMsg;
    }

    public String getExeResult() {
        return exeResult;
    }

    public synchronized String getUpdateMsg() {
        return updateMsg;
    }

    public String getRecentBox() {
        return recentBox;
    }

    public String getUpdateQual() {
        return updateQual;
    }

    public void resetQual() {
        updateQual = "";
    }

    public void resetExeResult() {
        exeResult = "";
    }

    public void resetRecentBox() {
        recentBox = "";
    }

    public void resetSwapDoneMsg() {
        swapDoneMsg = "";
    }

    public void resetQueryReply() {
        queryReply = "";
    }

    public void resetMsg() {
        msg = "";
    }

    public void resetLogginPerm() {
        isSignIn = false;
    }

    public synchronized void resetUpdateOnline() {
        updateOnline = "";
    }

    public synchronized void resetUpdateMsg() {
        updateMsg = "";
    }

    public class LocalBinder extends Binder {

        public LocalService getService() {
            return LocalService.this;
        }

    }

}
