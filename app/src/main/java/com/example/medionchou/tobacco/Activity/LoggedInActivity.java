package com.example.medionchou.tobacco.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.MarqueeTextView;
import com.example.medionchou.tobacco.ParentFragment.LookUpFragment;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class LoggedInActivity extends FragmentActivity implements ServiceListener {

    private LocalServiceConnection mConnection;

    private LocalService mService;

    private MarqueeTextView runningTextView;

    private RunningTextThread thread;

    private Timer logoutTimer;

    private final int TIMEOUT = 300000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        initObject();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        /*if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }*/
        thread = new RunningTextThread();
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
        thread.stopThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocalService.class));
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        //Log.v("MyLog", "ResetLogout");
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
        String msg;
        String oldMsg = "";

        @Override
        public void run() {
            super.run();
            LocalService mService;
            while (!mConnection.isBound()) {

            }
            mService = mConnection.getService();
            while (!stop) {
                try {
                    msg = mService.getMsg();
                    if (msg.length() > 0 && !oldMsg.equals(msg)) {
                        Log.v("MyLog", "Invoked");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runningTextView.setText(msg);
                            }
                        });
                    } else if (msg.length() > 0){

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
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("MyLog", e.toString());
                }
            }
        }

        public void stopThread() {
            stop = true;
        }
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
            return tabTitles[position];
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
            } catch(InterruptedException e) {

            }
        }
    }

}
