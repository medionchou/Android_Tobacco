package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by medionchou on 2015/8/23.
 */
public class DeviceFragment extends Fragment {

    private LocalServiceConnection mConnection;
    private DeviceStatusTask asynTask;
    private HashMap<String, Integer> deviceStatue;
    private List<String> sequences;
    private TableLayout first_col;
    private TableLayout second_col;


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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_device_layout, container, false);
        first_col = (TableLayout) rootView.findViewById(R.id.first_col_table_layout);
        second_col = (TableLayout) rootView.findViewById(R.id.second_col_table_layout);

        first_col.setStretchAllColumns(true);
        second_col.setStretchAllColumns(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        asynTask = new DeviceStatusTask();
        deviceStatue = new HashMap<>();
        sequences = new ArrayList<>();
        asynTask.execute((Void) null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        asynTask.cancel(true);
    }

    private class DeviceStatusTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.getting_online_state));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LocalService mService = mConnection.getService();
                String onlineStateMsg;

                mService.setCmd(Command.QUERY_ONLINE_STATE);
                Thread.sleep(2000);

                onlineStateMsg = mService.getQueryReply();

                parseOnlineStateMsg(onlineStateMsg);

                mService.resetQueryReply();

                publishProgress((Void) null);


                if (deviceStatue.size() > 0) {
                    while (!isCancelled()) {
                        String updateOnlineMsg = mService.getUpdateOnlineMsg();
                        if (updateOnlineMsg.length() > 0) {
                            updateOnlineState(updateOnlineMsg);
                            publishProgress((Void) null);

                            mService.resetUpdateOnline();
                        }
                        Thread.sleep(10000);
                    }
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", "InterruptedException In DeviceStatusTask: " + e.toString());
            }

            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            updateGUI();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }


        private void parseOnlineStateMsg(String onlineState) {
            String[] data = onlineState.split("\\t|<N>|<END>");
            for (int i = 0; i < data.length; i = i + 3) {
                deviceStatue.put(data[i + 1], Integer.valueOf(data[i + 2]));
                sequences.add(data[i+1]);
            }
        }

        private void updateOnlineState(String updateOnline) {
            String[] data = updateOnline.split("\\t|<END>");
            deviceStatue.put(data[1], Integer.valueOf(data[2]));
        }

        private void updateGUI() {
            first_col.removeAllViews();
            second_col.removeAllViews();
            for (int i = 0; i < sequences.size(); i++) {
                String deviceName = sequences.get(i);

                if (i % 2 == 0) {
                    inflateViewToLayout(deviceName, first_col);
                } else {
                    inflateViewToLayout(deviceName, second_col);
                }
            }
        }

        private void inflateViewToLayout(String deviceName, TableLayout tableLayout) {
            TableRow tableRow = new TableRow(getActivity());
            ImageView imageView = new ImageView(getActivity());
            TextView textView = new TextView(getActivity());

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams imageViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);


            tableRowParams.setMargins(1, 0, 1, 1);
            tableRow.setBackgroundColor(Color.GRAY);
            tableRow.setLayoutParams(tableRowParams);

            if (deviceStatue.get(deviceName) == 0) {
                imageView.setImageResource(R.drawable.red_cross);
            } else {
                imageView.setImageResource(R.drawable.green_circle);
            }
            imageViewParams.setMargins(0, 1, 0, 0);
            imageView.setLayoutParams(imageViewParams);

            textView.setText(deviceName);
            textView.setTextSize(Config.TEXT_SIZE);
            textView.setLayoutParams(textViewParams);

            tableRow.addView(imageView);
            tableRow.addView(textView);

            tableLayout.addView(tableRow);
        }
    }
}
