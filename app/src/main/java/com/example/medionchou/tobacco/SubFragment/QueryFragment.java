package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.BoringLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.ProductInfo;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Medion on 2015/8/25.
 */
public class QueryFragment extends Fragment {


    private LocalService mService;
    private String cmd = "";
    private QueryResultTask queryTask;
    private LinearLayout linearLayout;
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
        queryTask = new QueryResultTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.query_frag_layout, container, false);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_container);
        if (getArguments().getBoolean("LOOK_UP")) {
            createLookUpView();
        } else {
            setCommand(year, month, date);
        }
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
        String house, type;
        house = getArguments().getString("HOUSE_NAME");
        type = getArguments().getString("QUERY_TYPE");

        if (type.equals("HISTORY")) {
            if (house.equals("3號倉庫")) {
                cmd = Command.WH_HISTORY_THREE + year +  "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_HISTORY_FIVE + year +  "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_HISTORY_SIX + year +  "\t" + month + "\t" + date + "<END>";
            } else {
                cmd = Command.SH_HISTORY + year +  "\t" + month + "\t" + date + "<END>";
            }
        } else {
            if (house.equals("3號倉庫")) {
                cmd = Command.WH_NOW_THREE + year +  "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_NOW_FIVE + year +  "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_NOW_SIX + year +  "\t" + month + "\t" + date + "<END>";
            } else {
                cmd = Command.SH_NOW;
            }
        }
    }

    private class QueryResultTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        List<ProductInfo> productInfos = new ArrayList<>();
        boolean showDialog = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!isCancelled()) {
                    String stockMsg;
                    if (cmd.length() > 0) {
                        mService.setCmd(cmd);
                        showDialog = true;
                        publishProgress();
                        Thread.sleep(2000);
                        stockMsg = mService.getQueryReply();

                        if (cmd.contains("HISTORY")) {
                            parseHistoryMsg(stockMsg);
                        } else if (cmd.contains("NOW")) {
                            if (cmd.contains("WH_NOW")) {
                                parseNowMsg(stockMsg);
                            } else if (cmd.contains("SH_NOW")) {

                            }

                        }

                        mService.resetQueryReply();

                        cmd = "";
                    }

                    showDialog = false;

                    publishProgress((Void)null);
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", "InterruptedException In QueryResultTask: " +  e.toString());
            }


            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (showDialog) {
                setProgressDialog();
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

        private void setProgressDialog() {
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.getting_query_result));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        private void parseHistoryMsg(String stockMsg) {
            String[] data = stockMsg.split("\\t|<N>|<END>");

            for (int i = 0; i < data.length; i++) {
                Log.v("MyLog", data[i]);
            }
        }

        private void parseNowMsg(String stockMsg) {
            String[] data = stockMsg.split("\\t|<N>|<END>");

            for (int i = 0; i < data.length; i=i+5) {
                ProductInfo info = new ProductInfo(data[i+1], data[i+2], data[i+3], data[i+4]);
                Log.v("MyLog", data[i+1] + " "+ data[i+2]+ " "+ data[i+3]+ " "+ data[i+4]);
                productInfos.add(info);
            }
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

    private class SetCommandListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setCommand(year, month, date);

            Log.v("MyLog", "Test: " + year + "/" + month + "/" + date);
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
            QueryFragment.year = year;
            QueryFragment.month = monthOfYear + 1;
            QueryFragment.date = dayOfMonth;

            String dateInfo = QueryFragment.year + "/" +  QueryFragment.month + "/" + QueryFragment.date;
            dateTextView.setText(dateInfo);

        }
    }
}
