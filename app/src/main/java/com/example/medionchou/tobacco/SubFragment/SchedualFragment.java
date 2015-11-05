package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.Schedule;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Medion on 2015/11/4.
 */
public class SchedualFragment extends Fragment {

    private LocalService mService;
    private ScheduleAsync schedulAsync;
    private TableLayout tableLayout;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ServiceListener mCallBack;
        LocalServiceConnection mConnection;
        mCallBack = (ServiceListener) activity;
        mConnection = mCallBack.getLocalServiceConnection();
        mService = mConnection.getService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_schedule_layout, container, false);
        tableLayout = (TableLayout) rootView.findViewById(R.id.schedule_table_layout);
        tableLayout.setStretchAllColumns(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        schedulAsync = new ScheduleAsync();
        schedulAsync.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!schedulAsync.isCancelled())
            schedulAsync.cancel(true);
    }

    private class ScheduleAsync extends AsyncTask<Void, String, Void> {
        ProgressDialog progressDialog;
        List<Schedule> scheduleList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.getting_query_result));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String reply = "";

            try{
                while (!isCancelled()) {

                    while (reply.length() == 0) {
                        mService.setCmd(Command.SCHEDULE);
                        Thread.sleep(2000);
                        reply = mService.getQueryReply();
                        parseSchedule(reply);
                        publishProgress("Update");
                    }

                    if (progressDialog.isShowing())
                        publishProgress("");

                    Thread.sleep(2000);
                }

            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            if (values[0].equals("Update")) {
                inflateView();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void parseSchedule(String rawData) {
            String[] officeInfo = rawData.split("<N>|<END>");

            for (int i = 0; i < officeInfo.length; i++) {
                String[] detailOffice = officeInfo[i].split("\\t", -1);
                Schedule schedule;
                if (detailOffice[1].equals("1"))
                    schedule = new Schedule(true, detailOffice[2], detailOffice[3], detailOffice[4]);
                else
                    schedule = new Schedule(false, detailOffice[2], detailOffice[3], detailOffice[4]);

                scheduleList.add(schedule);
            }
        }

        public void inflateView() {
            for (int i = 0; i < scheduleList.size(); i=i+4) {
                addTableRow(i);
            }
        }

        public void addTableRow(int index) {
            TableRow tableRow = new TableRow(getActivity());
            RelativeLayout[] gridItem = new RelativeLayout[4];
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
            TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
            tableRow.setLayoutParams(tableLayoutParams);

            for (int i = 0; i < 4; i++) {
                int itemIndex = i + index;
                Schedule schedule = scheduleList.get(itemIndex);


                gridItem[i] = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.gridview_item, null);
                gridItem[i].setLayoutParams(tableRowParams);

                if (schedule.getOn())
                    gridItem[i].setBackgroundColor(Color.GREEN);
                else
                    gridItem[i].setBackgroundColor(Color.GRAY);

                ((TextView)gridItem[i].findViewById(R.id.office_text_view)).setText(schedule.getOffice());
                ((TextView)gridItem[i].findViewById(R.id.production_text_view)).setText(schedule.getProduction());
                ((TextView)gridItem[i].findViewById(R.id.onduty_text_view)).setText(schedule.getStaff());

                ((TextView)gridItem[i].findViewById(R.id.office_text_view)).setTextSize(Config.TEXT_SIZE);
                ((TextView)gridItem[i].findViewById(R.id.production_text_view)).setTextSize(Config.TEXT_SIZE);
                ((TextView)gridItem[i].findViewById(R.id.onduty_text_view)).setTextSize(20);

                tableRow.addView(gridItem[i]);
            }
            tableLayout.addView(tableRow);
        }
    }

}
