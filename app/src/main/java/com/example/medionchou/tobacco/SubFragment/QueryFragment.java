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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;

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
    private LinearLayout linearLayout;
    private static int year;
    private static int month;
    private static int date;
    private static Button button;

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
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        setCommand(year, month, date);
        queryTask = new QueryResultTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.query_frag_layout, container, false);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_container);
        if (getArguments().getBoolean("LOOK_UP")) {
            createDateButton();
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

    private void createDateButton() {
        button = new Button(getActivity());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
        String dateInfo = year + "/" + month + "/" + date;
        button.setText(dateInfo);
        button.setTextSize(40);
        button.setLayoutParams(buttonParams);
        button.setOnClickListener(new DatePickerTrigger(button));
        linearLayout.addView(button);
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

                mService.resetQueryReply();

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

    private class DatePickerTrigger implements View.OnClickListener {
        private Button button;

        public DatePickerTrigger(Button button) {
            this.button = button;
        }
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

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            QueryFragment.year = year;
            QueryFragment.month = monthOfYear + 1;
            QueryFragment.date = dayOfMonth;

            String dateInfo = QueryFragment.year + "/" +  QueryFragment.month + "/" + QueryFragment.date;
            button.setText(dateInfo);
        }
    }
}
