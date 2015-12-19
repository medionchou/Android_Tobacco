package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.MSData;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Medion on 2015/11/11.
 */
public class ASHistoryFragment extends Fragment {

    private int YEAR;
    private int MONTH;
    private int DATE;

    private QueryAsyncTask queryAsyncTask;
    private LocalService mService;
    private String cmd = "";
    private LinearLayout linearLayout;
    private TableLayout tableLayout;
    private static int year;
    private static int month;
    private static int date;

    private static TextView dateTextView;
    private Button dateButton;
    private Button sendButton;

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
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        YEAR = year;
        MONTH = month;
        DATE = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_query_layout, container, false);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_container);
        tableLayout = (TableLayout) rootView.findViewById(R.id.table_layout_container);
        tableLayout.setStretchAllColumns(true);

        createLookUpView();

        queryAsyncTask = new QueryAsyncTask();
        queryAsyncTask.execute((Void)null);
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

        if (!queryAsyncTask.isCancelled())
            queryAsyncTask.cancel(true);
    }

    private void createLookUpView() {
        dateButton = new Button(getActivity());
        sendButton = new Button(getActivity());
        dateTextView = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 20, 0, 0);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        String dateInfo = year + "/" + month + "/" + date;
        dateButton.setText("選擇查詢日期");
        dateButton.setTextSize(40);
        dateButton.setLayoutParams(layoutParams);
        dateButton.setOnClickListener(new DatePickerTrigger());

        layoutParams.setMargins(100, 0, 0, 0);
        dateTextView.setText(dateInfo);
        dateTextView.setTextSize(40);
        dateTextView.setLayoutParams(layoutParams);

        sendButton.setText("送出");
        sendButton.setTextSize(40);
        sendButton.setLayoutParams(layoutParams);
        sendButton.setOnClickListener(new SetCommandListener());


        linearLayout.addView(dateButton);
        linearLayout.addView(dateTextView);
        linearLayout.addView(sendButton);
    }

    private void setCommand(int year, int month, int date) {
        cmd = Command.AS_HISTORY + year + "\t" + month + "\t" + date + "\t" + year + "\t" + month + "\t" + date + "<END>";
    }

    private class QueryAsyncTask extends AsyncTask<Void, String, Void> {

        ProgressDialog progressDialog;
        List<MSData> msDataList = new ArrayList<>();
        List<String> updateMsgQueue = new LinkedList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            String query = "";
            String updateMsg = "";
            String msg = "";

            try {
                while (!isCancelled()) {

                    if (cmd.length() > 0) {


                        mService.setCmd(cmd);
                        publishProgress("", "ShowDialog", "", "");

                        Thread.sleep(2000);
                        query = mService.getQueryReply();

                        mService.resetQueryReply();

                        if (query.length() > 0) {
                            parseSWAP(query, false);
                            msg = "AS_HISTORY";
                        } else {
                            publishProgress("", "ShowDialog", "警告", "查無資料");
                            if (msDataList.size() > 0)
                                msDataList.clear();
                        }

                        publishProgress(msg, "", "", "");
                        cmd = "";
                    }

                    updateMsg = mService.getUpdateMsg();

                    if (updateMsg.length() > 0) {
                        mService.resetUpdateMsg();
                        parseUpdateMsg(updateMsg);

                        for (int i = 0; i < updateMsgQueue.size(); i++) {
                            String text = updateMsgQueue.get(i);

                            if (text.contains("AS_HISTORY") && isTimeMatch()) {
                                parseSWAP(text, true);
                            }
                        }

                        publishProgress(msg, "", "", "");
                        updateMsgQueue.clear();
                    }

                    Thread.sleep(500);
                }
            } catch(InterruptedException e) {
                Log.e("MyLog", "InterruptedException in ASHistoryFragment " + e.toString());
            }
            return (Void)null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("AS_HISTORY"))
                inflateView();

            if (values[1].equals("ShowDialog")) {
                if (!values[2].equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(values[2]);
                    builder.setMessage(values[3]);
                    builder.show();
                } else {
                    progressDialog.setTitle(R.string.progress_dialog_waiting);
                    progressDialog.setMessage(getString(R.string.getting_query_result));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            } else {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

        private boolean isTimeMatch() {
            return YEAR == year && MONTH == month && DATE == date;
        }

        private void parseSWAP(String rawData, boolean update) {
            String[] detailMS = rawData.split("<N>|<END>");

            if (update) {
                MSData msData = new MSData(detailMS[1], detailMS[2], detailMS[3], detailMS[4], detailMS[5]);
                msDataList.add(msData);

            } else {
                if (msDataList.size() > 0)
                    msDataList.clear();

                for (int i = 0; i < detailMS.length; i = i + 1) {
                    String[] tmp = detailMS[i].split("\\t", -1);
                    MSData msData = new MSData(tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]);
                    msDataList.add(msData);
                }
            }
        }

        private void parseUpdateMsg(String updateMsg) {
            String data[] = updateMsg.split("<END>");

            for (int i = 0; i < data.length; i++) {
                updateMsgQueue.add(data[i]);
            }
        }

        private void inflateView() {
            tableLayout.removeAllViews();
            addTableTitle();
            for (int i = 0; i < msDataList.size(); i++) {
                addTableRow(i);
            }
        }

        private void addTableRow(int i) {
            TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.ms_row_item, null);
            TextView time = (TextView) tableRow.findViewById(R.id.time);
            TextView rID = (TextView) tableRow.findViewById(R.id.rID);
            TextView rName = (TextView) tableRow.findViewById(R.id.rName);
            TextView staffID = (TextView) tableRow.findViewById(R.id.staffID);
            TextView staff = (TextView) tableRow.findViewById(R.id.staff);
            MSData msData = msDataList.get(i);

            time.setText(msData.getTime());
            rID.setText(msData.getrID());
            rName.setText(msData.getrName());
            staffID.setText(msData.getStaffID());
            staff.setText(msData.getStaff());

            time.setTextSize(Config.TEXT_SIZE);
            rID.setTextSize(Config.TEXT_SIZE);
            rName.setTextSize(Config.TEXT_SIZE);
            staffID.setTextSize(Config.TEXT_SIZE);
            staff.setTextSize(Config.TEXT_SIZE);

            time.setMaxEms(5);

            tableLayout.addView(tableRow);
        }

        private void addTableTitle() {
            TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.ms_row_item, null);
            TextView time = (TextView) tableRow.findViewById(R.id.time);
            TextView rID = (TextView) tableRow.findViewById(R.id.rID);
            TextView rName = (TextView) tableRow.findViewById(R.id.rName);
            TextView staffID = (TextView) tableRow.findViewById(R.id.staffID);
            TextView staff = (TextView) tableRow.findViewById(R.id.staff);

            time.setText("時間");
            rID.setText("配方名稱");
            rName.setText("流水編號");
            staffID.setText("員工姓名");
            staff.setText("備註");

            time.setTextSize(Config.TEXT_TITLE_SIZE);
            rID.setTextSize(Config.TEXT_TITLE_SIZE);
            rName.setTextSize(Config.TEXT_TITLE_SIZE);
            staffID.setTextSize(Config.TEXT_TITLE_SIZE);
            staff.setTextSize(Config.TEXT_TITLE_SIZE);

            time.setTypeface(null, Typeface.BOLD);
            rID.setTypeface(null, Typeface.BOLD);
            rName.setTypeface(null, Typeface.BOLD);
            staffID.setTypeface(null, Typeface.BOLD);
            staff.setTypeface(null, Typeface.BOLD);

            time.setTextColor(Color.BLACK);
            rID.setTextColor(Color.BLACK);
            rName.setTextColor(Color.BLACK);
            staffID.setTextColor(Color.BLACK);
            staff.setTextColor(Color.BLACK);


            tableLayout.addView(tableRow);
        }
    }

    private class SetCommandListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setCommand(year, month, date);

            Log.v("MyLog", "Test: " + year + "/" + month + "/" + date);
        }
    }

    private class DatePickerTrigger implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            DialogFragment dateFragment = new DatePickerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("YEAR", year);
            bundle.putInt("MONTH", month);
            bundle.putInt("DATE", date);
            dateFragment.setArguments(bundle);
            dateFragment.show(getChildFragmentManager(), "datePicker");
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year;
            int month;
            int day;
            if (getArguments() == null) {
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                year = getArguments().getInt("YEAR");
                month = getArguments().getInt("MONTH") - 1;
                day = getArguments().getInt("DATE");
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setCalendarViewShown(false);
            datePickerDialog.getDatePicker().setScaleX(2);
            datePickerDialog.getDatePicker().setScaleY(2);
            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            ASHistoryFragment.year = year;
            ASHistoryFragment.month = monthOfYear + 1;
            ASHistoryFragment.date = dayOfMonth;

            String dateInfo = ASHistoryFragment.year + "/" + ASHistoryFragment.month + "/" + ASHistoryFragment.date;
            dateTextView.setText(dateInfo);

        }
    }

}
