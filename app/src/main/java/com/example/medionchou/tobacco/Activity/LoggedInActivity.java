package com.example.medionchou.tobacco.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.LineState;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.DataContainer.RecipeList;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.MarqueeTextView;
import com.example.medionchou.tobacco.ParentFragment.LookUpFragment;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;
import com.example.medionchou.tobacco.SubFragment.CPFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LoggedInActivity extends FragmentActivity implements ServiceListener {

    private LocalServiceConnection mConnection;

    private LocalService mService;

    private MarqueeTextView runningTextView;

    private RunningTextThread thread;

    private Timer logoutTimer;

    private String workerId;

    private int TIMEOUT = 300000;

    private GetSwapThread swapThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        Bundle extras = getIntent().getExtras();
        workerId = extras.getString("WorkerId");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initObject();
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        /*if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }*/
        thread = new RunningTextThread();
        thread.start();

        swapThread = new GetSwapThread();
        swapThread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        thread.stopThread();
        thread = null;
        logoutTimer.cancel();

        swapThread.stopThread();
        swapThread = null;

        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocalService.class));
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        Log.v("MyLog", "ResetLogout");
        logoutTimer.cancel();
        logoutTimer = new Timer();

        logoutTimer.schedule(new LogoutTimerTask(), TIMEOUT);
    }

    private void initObject() {
        mConnection = new LocalServiceConnection();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        runningTextView = (MarqueeTextView) findViewById(R.id.running_text_view);
        //thread = new RunningTextThread();
        logoutTimer = new Timer();
        logoutTimer.schedule(new LogoutTimerTask(), TIMEOUT);
    }


    public LocalServiceConnection getLocalServiceConnection() {
        return mConnection;
    }


    private class RunningTextThread extends Thread {

        private boolean stop = false;
        String msg = "";
        String oldMsg = "";

        @Override
        public void run() {
            super.run();
            LocalService mService;

            while (!mConnection.isBound()) ;

            mService = mConnection.getService();

            while (!stop) {
                try {
                    msg = mService.getMsg();
                    if (msg.length() > 0 && !oldMsg.equals(msg)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runningTextView.setText(msg);
                            }
                        });
                    } else if (msg.length() > 0) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar cal = Calendar.getInstance();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                String date = dateFormat.format(cal.getTime());
                                runningTextView.setNewText(msg + date);
                            }
                        });

                    }
                    oldMsg = msg;
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.e("MyLog", e.toString());
                }
            }
        }

        public void stopThread() {
            stop = true;
        }
    }

    public String getWorkerId() {
        return workerId;
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 1;
        private String[] tabTitles = {"主管查詢"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return LookUpFragment.newInstance(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return null;
        }
    }

    private class LogoutTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                mService = mConnection.getService();
                mService.setCmd("LOGOUT<END>");
                Thread.sleep(1000);
                Intent intent = new Intent(LoggedInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("LOGOUT", true);
                startActivity(intent);
            } catch (InterruptedException e) {

            }
        }
    }

    private class GetSwapThread extends Thread {

        private boolean stop = false;
        AlertDialog.Builder requestBuilder;
        AlertDialog request;

        public GetSwapThread() {
            requestBuilder = new AlertDialog.Builder(LoggedInActivity.this);
            request = requestBuilder.create();
        }

        @Override
        public void run() {
            super.run();
            try {
                String exeRes;

                while (mConnection == null) {
                    Thread.sleep(500);
                }

                while (!mConnection.isBound()) {
                    Thread.sleep(500);
                }

                mService = mConnection.getService();

                while (!stop) {
                    exeRes = mService.getExeResult();

                    if (exeRes.length() > 0) {
                        if (exeRes.contains("EXE_SWAP")) {
                            final String msg = exeRes;
                            if (!request.isShowing()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!request.isShowing()) {
                                            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_swap, null);
                                            String tmp = msg;
                                            tmp = tmp.replace("EXE_SWAP\t", "");
                                            tmp = tmp.replace("<END>", "");

                                            request.setTitle("請注意");
                                            request.setView(linearLayout);
                                            request.setCanceledOnTouchOutside(false);

                                            ((TextView) linearLayout.findViewById(R.id.message_text)).setText(tmp);
                                            ((TextView) linearLayout.findViewById(R.id.message_text)).setTextColor(Color.BLACK);

                                            ((Button) linearLayout.findViewById(R.id.confirm)).setOnClickListener(new SwapCheckListener(false, linearLayout, request));
                                            ((Button) linearLayout.findViewById(R.id.cancel)).setOnClickListener(new SwapCheckListener(true, linearLayout, request));

                                            request.show();
                                        }

                                    }
                                });
                            }
                            onUserInteraction();
                        }
                    }

                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString());
            }
        }

        public void stopThread() {
            stop = true;
        }
    }

    private class SwapCheckListener implements View.OnClickListener {

        boolean isCancel;
        LinearLayout linearLayout;
        AlertDialog dialog;

        public SwapCheckListener(boolean isCancel, LinearLayout linearLayout, AlertDialog dialog) {
            this.isCancel = isCancel;
            this.linearLayout = linearLayout;
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            if (!isCancel) {
                String inputId = ((EditText) linearLayout.findViewById(R.id.worker_text)).getText().toString();
                String workerId = getWorkerId();


                if (workerId.equals(inputId)) {
                    mService.setCmd("EXE_SWAP_OK<END>");
                    dialog.dismiss();
                    mService.resetExeResult();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoggedInActivity.this);

                    builder.setTitle("警告");
                    builder.setMessage("輸入編號必須與登入編號相同");
                    builder.show();
                }
            } else {
                mService.setCmd("EXE_SWAP_CANCEL<END>");
                dialog.dismiss();
                mService.resetExeResult();
            }
        }
    }

}
