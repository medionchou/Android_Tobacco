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
import android.util.Log;
import android.widget.TextView;

import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.ParentFragment.LookUpFragment;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;


public class LoggedInActivity extends FragmentActivity implements ServiceListener {

    private LocalServiceConnection mConnection;
    private LocalService mService;

    private TextView runningTextView;

    private RunningTextThread thread;


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
        if (thread.getState() == Thread.State.NEW)
            thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
        thread.stopThread();


        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initObject() {
        mConnection = new LocalServiceConnection();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        runningTextView = (TextView) findViewById(R.id.running_text_view);
        thread = new RunningTextThread();
    }


    public LocalServiceConnection getLocalServiceConnection() {
        return mConnection;
    }


    private class RunningTextThread extends Thread {

        private boolean stop = false;
        String msg;

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
                    if (msg.length() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runningTextView.setText(msg);
                            }
                        });
                        mService.resetMsg();
                    }

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

}
