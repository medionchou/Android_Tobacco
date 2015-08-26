package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.Calendar;

/**
 * Created by Medion on 2015/8/25.
 */
public class QueryFragment extends Fragment {


    private LocalServiceConnection mConnection;
    private String cmd = "";
    private QueryResultTask queryTask;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ServiceListener mCallBack;
        mCallBack = (ServiceListener) activity;
        mConnection = mCallBack.getLocalServiceConnection();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCommand();
        queryTask = new QueryResultTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.query_frag_layout, container, false);
        queryTask.execute((Void)null);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        queryTask.cancel(true);
    }


    private void setCommand() {
        String house, type;
        Calendar calendar;
        house = getArguments().getString("HOUSE_NAME");
        type = getArguments().getString("QUERY_TYPE");
        calendar = Calendar.getInstance();

        if (type.equals("HISTORY")) {
            if (house.equals("3號倉庫")) {
                cmd = Command.WH_HISTORY_THREE + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_HISTORY_FIVE + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_HISTORY_SIX + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else {
                cmd = Command.SH_HISTORY + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            }
        } else {
            if (house.equals("3號倉庫")) {
                cmd = Command.WH_NOW_THREE + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_NOW_FIVE + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_NOW_SIX + calendar.get(Calendar.YEAR) +  "\t" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "\t" + calendar.get(Calendar.DATE) + "<END>";
            } else {
                cmd = Command.SH_NOW;
            }
        }
    }

    private class QueryResultTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.getting_query_result));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LocalService mService = mConnection.getService();
                String stockMsg;
                mService.setCmd(cmd);
                Thread.sleep(2000);

                stockMsg = mService.getQueryReply();

                Log.v("MyLog", stockMsg);

            } catch (InterruptedException e) {
                Log.e("MyLog", "InterruptedException In QueryResultTask: " +  e.toString());
            }


            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

    }
}
