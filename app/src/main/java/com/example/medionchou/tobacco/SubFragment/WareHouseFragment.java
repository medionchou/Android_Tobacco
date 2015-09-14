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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.DataContainer.WareHouseInfo;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;
import com.example.medionchou.tobacco.DataContainer.SideHouse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Medion on 2015/8/25.
 */
public class WareHouseFragment extends Fragment {


    private int YEAR;
    private int MONTH;
    private int DATE;

    private LocalService mService;
    private String cmd = "";
    private QueryResultTask queryTask;
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
        queryTask = new QueryResultTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_query_layout, container, false);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_container);
        tableLayout = (TableLayout) rootView.findViewById(R.id.table_layout_container);

        tableLayout.setStretchAllColumns(true);

        if (getArguments().getBoolean("LOOK_UP")) {
            createLookUpView();
        } else {
            setCommand(year, month, date);
        }
        queryTask.execute((Void) null);
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
        if (!queryTask.isCancelled()) {
            queryTask.cancel(true);
        }
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
                cmd = Command.WH_HISTORY_THREE + year + "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_HISTORY_FIVE + year + "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_HISTORY_SIX + year + "\t" + month + "\t" + date + "<END>";
            } else {
                cmd = Command.SH_HISTORY + year + "\t" + month + "\t" + date + "<END>";
            }
        } else {
            if (house.equals("3號倉庫")) {
                cmd = Command.WH_NOW_THREE + year + "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("5號倉庫")) {
                cmd = Command.WH_NOW_FIVE + year + "\t" + month + "\t" + date + "<END>";
            } else if (house.equals("6號倉庫")) {
                cmd = Command.WH_NOW_SIX + year + "\t" + month + "\t" + date + "<END>";
            } else {
                cmd = Command.SH_NOW;
            }
        }
    }

    private class QueryResultTask extends AsyncTask<Void, String, Void> {

        ProgressDialog progressDialog;
        List<SideHouse> sideHouseList = new ArrayList<>();
        List<WareHouseInfo> wareHouseInfoList = new ArrayList<>();
        List<String> updateMsgQueue = new LinkedList<>();
        String msg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!isCancelled()) {
                    String stockMsg;
                    String updateMsg;

                    if (cmd.length() > 0) {
                        mService.setCmd(cmd);
                        publishProgress("", "ShowDialog", "", "");
                        Thread.sleep(2000);

                        stockMsg = mService.getQueryReply();
                        mService.resetQueryReply();

                        if (!stockMsg.contains("QUERY_NULL<END>") && stockMsg.length() != 0) {
                            if (cmd.contains("HISTORY")) {
                                parseHistoryMsg(stockMsg, true);
                                if (cmd.contains("WH_HISTORY")) {
                                    msg = "WH_HISTORY";
                                } else if (cmd.contains("SH_HISTORY")) {
                                    msg = "SH_HISTORY";
                                }
                            } else if (cmd.contains("NOW")) {
                                if (cmd.contains("WH_NOW")) {
                                    parseWH_NOWmsg(stockMsg, false);
                                    msg = "WH_NOW";
                                } else if (cmd.contains("SH_NOW")) {
                                    msg = "SH_NOW";
                                    parseSH_NOWmsg(stockMsg, false);
                                }
                            }
                        } else {
                            publishProgress("", "ShowDialog", "警告", "查無資料");
                        }

                        publishProgress(msg, "", "", "");
                        cmd = "";
                    }

                    updateMsg = mService.getUpdateMsg();
                    if (updateMsg.length() > 0 && isTimeMatch()) {
                        mService.resetUpdateMsg();
                        parseUpdateMsg(updateMsg);

                        for (int i = 0; i < updateMsgQueue.size(); i++) {
                            String text = updateMsgQueue.get(i);
                            if (msg.equals("WH_HISTORY") && text.contains("WH_HISTORY")) {
                                parseHistoryMsg(text, false);

                            } else if (msg.equals("SH_HISTORY") && text.contains("SH_HISTORY")) {
                                parseHistoryMsg(text, false);

                            } else if (msg.equals("WH_NOW") && text.contains("WH_NOW")) {
                                parseWH_NOWmsg(text, true);

                            } else if (msg.equals("SH_NOW") && text.contains("SH_NOW")) {
                                parseSH_NOWmsg(text, true);
                            }
                        }
                        publishProgress(msg, "", "", "");
                        updateMsgQueue.clear();
                    }
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", "InterruptedException In QueryResultTask: " + e.toString());
            }


            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("WH_NOW")) {
                updateWH_NOWgui(wareHouseInfoList);
            } else if (values[0].equals("SH_NOW")) {
                updateSH_NOWgui(sideHouseList);
            } else if (values[0].contains("HISTORY")) {
                updateHistorygui(wareHouseInfoList);
            }

            if (values[1].equals("ShowDialog")) {
                if (!values[2].equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(values[2]);
                    builder.setMessage(values[3]);
                    builder.show();
                } else {
                    progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
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

        private void parseHistoryMsg(String stockMsg, boolean clear) {
            /**
             *  TODO: empty command
             */
            String[] data = stockMsg.split("\\t|<N>|<END>");

            if (clear) {
                wareHouseInfoList.clear();
            }

            for (int i = 0; i < data.length; i = i + 8) {
                WareHouseInfo info = new WareHouseInfo(data[i + 1], data[i + 2], data[i + 3], data[i + 4], data[i + 5], data[i + 6], data[i + 7]);
                wareHouseInfoList.add(info);
            }
        }

        private void parseWH_NOWmsg(String stockMsg, boolean update) {
            /**
             *  TODO: empty command
             */

            String[] data = stockMsg.split("\\t|<N>|<END>");


            if (update) {
                WareHouseInfo info = new WareHouseInfo(data[1], data[2], data[3], data[4]);
                boolean isUpdate = false;
                for (int i = 0; i < wareHouseInfoList.size(); i++) {
                    WareHouseInfo tmp = wareHouseInfoList.get(i);
                    if (tmp.isProductIdMatch(info)) {
                        isUpdate = true;
                        wareHouseInfoList.set(i, info);
                    }
                }

                if (!isUpdate)
                    wareHouseInfoList.add(info);

            } else {
                for (int i = 0; i < data.length; i = i + 5) {
                    WareHouseInfo info = new WareHouseInfo(data[i + 1], data[i + 2], data[i + 3], data[i + 4]);
                    wareHouseInfoList.add(info);
                }
            }
        }

        private void parseSH_NOWmsg(String stockMsg, boolean update) {

            String[] data = stockMsg.split("\\t|<N>|<END>");

            if (update) {
                SideHouse sideHouse = new SideHouse(data[1], data[2], data[3]);

                for (int i = 0; i < sideHouseList.size(); i++) {
                    SideHouse tmp = sideHouseList.get(i);
                    if (tmp.isNameMatch(sideHouse)) {
                        sideHouseList.set(i, sideHouse);
                    }
                }
            }
            else {
                for (int i = 0; i < data.length; i = i + 4) {
                    SideHouse sideHouse = new SideHouse(data[i + 1], data[i + 2], data[i + 3]);
                    sideHouseList.add(sideHouse);
                }
            }
        }

        private void parseUpdateMsg(String updateMsg) {
            String data[] = updateMsg.split("<END>");

            for (int i = 0; i < data.length; i++) {
                updateMsgQueue.add(data[i]);
            }
        }


        private void updateWH_NOWgui(List<WareHouseInfo> wareHouseInfoList) {
            tableLayout.removeAllViews();

            for (int i = 0; i < wareHouseInfoList.size(); i++) {
                WareHouseInfo info = wareHouseInfoList.get(i);
                inflateTextView(info, i);
            }
        }

        private void updateSH_NOWgui(List<SideHouse> info) {
            tableLayout.removeAllViews();

            for (int i = 0; i < info.size(); i++) {
                SideHouse sideHouse = info.get(i);
                inflateTextView(sideHouse);
            }
        }

        private void updateHistorygui(List<WareHouseInfo> wareHouseInfoList) {
            tableLayout.removeAllViews();

            for (int i = 0; i < wareHouseInfoList.size(); i++) {
                WareHouseInfo info = wareHouseInfoList.get(i);
                inflateTextView(info, i);
            }
        }

        private void inflateTextView(WareHouseInfo info, int indexToInflate) {
            TableRow tableRow = new TableRow(getActivity());
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            TextView date = new TextView(getActivity());
            TextView action = new TextView(getActivity());
            TextView productId = new TextView(getActivity());
            TextView productName = new TextView(getActivity());
            TextView quantity = new TextView(getActivity());
            TextView unit = new TextView(getActivity());
            TextView person = new TextView(getActivity());


            tableRow.setLayoutParams(tableRowParams);
            date.setLayoutParams(textViewParams);
            action.setLayoutParams(textViewParams);
            productId.setLayoutParams(textViewParams);
            productName.setLayoutParams(textViewParams);
            quantity.setLayoutParams(textViewParams);
            unit.setLayoutParams(textViewParams);
            person.setLayoutParams(textViewParams);


            if (!info.getDate().equals("")) { //history
                date.setText(info.getDate());
                action.setText(info.getAction());
                productId.setText(info.getProductId());
                productName.setText(info.getProductName());
                quantity.setText(info.getQuantity());
                unit.setText(info.getUnit());
                person.setText(info.getPerson());


                date.setTextSize(Config.TEXT_SIZE);
                action.setTextSize(Config.TEXT_SIZE);
                productId.setTextSize(Config.TEXT_SIZE);
                productName.setTextSize(Config.TEXT_SIZE);
                quantity.setTextSize(Config.TEXT_SIZE);
                unit.setTextSize(Config.TEXT_SIZE);
                person.setTextSize(Config.TEXT_SIZE);


                if (indexToInflate == 0) {
                    setTitleLabel(true);
                }

                tableRow.addView(date);
                tableRow.addView(action);
                tableRow.addView(productId);
                tableRow.addView(productName);
                tableRow.addView(quantity);
                tableRow.addView(unit);
                tableRow.addView(person);
            } else { // now
                productId.setText(info.getProductId());
                productName.setText(info.getProductName());
                quantity.setText(info.getQuantity());
                unit.setText(info.getUnit());
                productId.setTextSize(Config.TEXT_SIZE);
                productName.setTextSize(Config.TEXT_SIZE);
                quantity.setTextSize(Config.TEXT_SIZE);
                unit.setTextSize(Config.TEXT_SIZE);

                if (indexToInflate == 0) {
                    setTitleLabel(false);
                }

                tableRow.addView(productId);
                tableRow.addView(productName);
                tableRow.addView(quantity);
                tableRow.addView(unit);
            }
            tableLayout.addView(tableRow);
        }

        private void setTitleLabel(boolean isHistory) {

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow titleRow = new TableRow(getActivity());
            TextView dateTitle = new TextView(getActivity());
            TextView actionTitle = new TextView(getActivity());
            TextView productIdTitle = new TextView(getActivity());
            TextView productNameTitle = new TextView(getActivity());
            TextView quantityTitle = new TextView(getActivity());
            TextView unitTitle = new TextView(getActivity());
            TextView personTitle = new TextView(getActivity());


            tableRowParams.setMargins(1, 10, 1, 1);
            titleRow.setLayoutParams(tableRowParams);
            dateTitle.setLayoutParams(textViewParams);
            actionTitle.setLayoutParams(textViewParams);
            productIdTitle.setLayoutParams(textViewParams);
            productNameTitle.setLayoutParams(textViewParams);
            quantityTitle.setLayoutParams(textViewParams);
            unitTitle.setLayoutParams(textViewParams);
            personTitle.setLayoutParams(textViewParams);

            dateTitle.setTextColor(Color.BLACK);
            actionTitle.setTextColor(Color.BLACK);
            productIdTitle.setTextColor(Color.BLACK);
            productNameTitle.setTextColor(Color.BLACK);
            quantityTitle.setTextColor(Color.BLACK);
            unitTitle.setTextColor(Color.BLACK);
            personTitle.setTextColor(Color.BLACK);

            dateTitle.setTypeface(null, Typeface.BOLD);
            actionTitle.setTypeface(null, Typeface.BOLD);
            productIdTitle.setTypeface(null, Typeface.BOLD);
            productNameTitle.setTypeface(null, Typeface.BOLD);
            quantityTitle.setTypeface(null, Typeface.BOLD);
            unitTitle.setTypeface(null, Typeface.BOLD);
            personTitle.setTypeface(null, Typeface.BOLD);


            if (isHistory) {
                dateTitle.setText("日期");
                actionTitle.setText("動作");
                productIdTitle.setText("產品編號");
                productNameTitle.setText("產品名稱");
                quantityTitle.setText("數量");
                unitTitle.setText("單位");
                personTitle.setText("人員");

                dateTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                actionTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                productIdTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                productNameTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                quantityTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                unitTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                personTitle.setTextSize(Config.TEXT_TITLE_SIZE);

                titleRow.addView(dateTitle);
                titleRow.addView(actionTitle);
                titleRow.addView(productIdTitle);
                titleRow.addView(productNameTitle);
                titleRow.addView(quantityTitle);
                titleRow.addView(unitTitle);
                titleRow.addView(personTitle);
            } else {

                productIdTitle.setText("產品編號");
                productNameTitle.setText("產品名稱");
                quantityTitle.setText("數量");
                unitTitle.setText("單位");

                productIdTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                productNameTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                quantityTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                unitTitle.setTextSize(Config.TEXT_TITLE_SIZE);

                titleRow.addView(productIdTitle);
                titleRow.addView(productNameTitle);
                titleRow.addView(quantityTitle);
                titleRow.addView(unitTitle);
            }

            tableLayout.addView(titleRow);
        }

        private void inflateTextView(SideHouse sideHouse) {
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow tableRow = new TableRow(getActivity());
            TextView name = new TextView(getActivity());
            TextView productName = new TextView(getActivity());
            TextView unit = new TextView(getActivity());

            tableRow.setLayoutParams(tableRowParams);
            name.setLayoutParams(rowParams);
            productName.setLayoutParams(rowParams);
            unit.setLayoutParams(rowParams);

            name.setText(sideHouse.getName());
            productName.setText(sideHouse.getProductName());
            unit.setText(sideHouse.getQuantity());

            name.setTextSize(Config.TEXT_SIZE);
            productName.setTextSize(Config.TEXT_SIZE);
            unit.setTextSize(Config.TEXT_SIZE);

            tableRow.addView(name);
            tableRow.addView(productName);
            tableRow.addView(unit);

            tableLayout.addView(tableRow);
        }

        private boolean isTimeMatch() {
            return YEAR == year && MONTH == month && DATE == date;
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
            WareHouseFragment.year = year;
            WareHouseFragment.month = monthOfYear + 1;
            WareHouseFragment.date = dayOfMonth;

            String dateInfo = WareHouseFragment.year + "/" + WareHouseFragment.month + "/" + WareHouseFragment.date;
            dateTextView.setText(dateInfo);

        }
    }
}
