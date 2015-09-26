package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.LineState;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.Activity.DetailDialogActivity;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Medion on 2015/9/7.
 * Cigarette and Packing
 */
public class CPFragment extends Fragment {

    private LocalService mService;
    private TableLayout tableLayout;

    private ProductAsynTask asyncTask;


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
        View rootView = inflater.inflate(R.layout.frag_product_layout, container, false);
        tableLayout = (TableLayout) rootView.findViewById(R.id.product_table_layout);

        tableLayout.setStretchAllColumns(true);
        asyncTask = new ProductAsynTask();
        asyncTask.execute((Void) null);
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

        if (!asyncTask.isCancelled())
            asyncTask.cancel(true);
    }


    private class ProductAsynTask extends AsyncTask<Void, String, Void> {
        ProgressDialog progressDialog;
        private List<ProductLine> productLineList = new ArrayList<>();
        private List<LineState> lineStateList = new ArrayList<>();

        private List<String> updateMsgQueue = new LinkedList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("請稍後");
            progressDialog.setMessage("取得生產資訊中");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String msg;
                String swapDoneMsg;

                mService.setCmd(Command.PRODUCT);
                Thread.sleep(2000);
                msg = mService.getQueryReply();
                parseProductLine(msg, false);
                mService.resetQueryReply();

                mService.setCmd(Command.SWAP);
                Thread.sleep(2000);
                msg = mService.getQueryReply();
                parseLineState(msg, false);
                mService.resetQueryReply();

                publishProgress("", "");


                while (!isCancelled()) {
                    msg = mService.getUpdateMsg();
                    swapDoneMsg = mService.getSwapDoneMsg();


                    if (msg.length() > 0) {
                        mService.resetUpdateMsg();
                        parseUpdateMsg(msg);

                        for (int i = 0; i < updateMsgQueue.size(); i++) {
                            String text = updateMsgQueue.get(i);

                            if (text.contains("SWAP")) {
                                parseLineState(text, true);
                            } else if (text.contains("PRODUCT")) {
                                parseProductLine(text, true);
                            }
                        }

                        publishProgress("", "");
                    }

                    if (swapDoneMsg.length() > 0) {
                        ProductLine productLine;

                        mService.resetSwapDoneMsg();
                        productLine = parseSwapDone(swapDoneMsg);

                        publishProgress(productLine.getCategory(), productLine.getLineNum());

                    }
                    Thread.sleep(5000);
                }

            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString());
            }
            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            updateGui(values[0], values[1]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void updateGui(String category, String lineNum) {
            tableLayout.removeAllViews();

            for (int i = 0; i < lineStateList.size(); ) {

                inflateView(i, category, lineNum);

                if (i < 16) {
                    i = i + 2;
                } else {
                    i++;
                }
            }
        }

        private void inflateView(int index, String category, String lineNum) {

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
            TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams viewParams_topMargin = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            TableRow tableRow = new TableRow(getActivity());
            TextView cur_production = new TextView(getActivity());
            Button production_serial = new Button(getActivity());
            TextView status = new TextView(getActivity());
            TextView status_cm = new TextView(getActivity());
            TextView status_pm = new TextView(getActivity());
            TextView statusTextView = new TextView(getActivity());
            TextView statusTextView_cm = new TextView(getActivity());
            TextView statusTextView_pm = new TextView(getActivity());
            Button swap = new Button(getActivity());
            Button broadcast = new Button(getActivity());

            if (index == 0) {
                setTitleLabel();
            }

            tableRowParams.setMargins(0, 20, 0, 0);
            viewParams_topMargin.setMargins(0, 10, 0, 0);

            tableRow.setLayoutParams(tableRowParams);
            cur_production.setLayoutParams(viewParams);
            production_serial.setLayoutParams(viewParams);
            status.setLayoutParams(viewParams);
            statusTextView.setLayoutParams(viewParams);
            swap.setLayoutParams(viewParams);
            broadcast.setLayoutParams(viewParams);

            if (index < 16) {
                LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout linearLayout_status = new LinearLayout(getActivity());
                LinearLayout linearLayout_statusText = new LinearLayout(getActivity());
                TextView name = new TextView(getActivity());

                childParams.setMargins(0, 2, 0, 2);

                linearLayout_status.setOrientation(LinearLayout.VERTICAL);
                linearLayout_status.setLayoutParams(viewParams);
                linearLayout_statusText.setOrientation(LinearLayout.VERTICAL);
                linearLayout_statusText.setLayoutParams(viewParams);

                name.setLayoutParams(viewParams_topMargin);
                cur_production.setLayoutParams(viewParams_topMargin);
                production_serial.setLayoutParams(viewParams_topMargin);
                status_cm.setLayoutParams(childParams);
                status_pm.setLayoutParams(childParams);
                statusTextView_cm.setLayoutParams(childParams);
                statusTextView_pm.setLayoutParams(childParams);
                swap.setLayoutParams(viewParams_topMargin);
                broadcast.setLayoutParams(viewParams_topMargin);

                status_cm.setGravity(Gravity.CENTER_HORIZONTAL);
                status_pm.setGravity(Gravity.CENTER_HORIZONTAL);
                statusTextView_cm.setGravity(Gravity.CENTER_HORIZONTAL);
                statusTextView_pm.setGravity(Gravity.CENTER_HORIZONTAL);

                name.setText("生產線_" + String.valueOf(index / 2 + 1));
                production_serial.setText("查看生產序列");
                status_cm.setText(lineStateList.get(index).getCategory() + "_" + lineStateList.get(index).getLineNum());
                status_pm.setText(lineStateList.get(index + 1).getCategory() + "_" + lineStateList.get(index + 1).getLineNum());
                statusTextView_cm.setText(lineStateList.get(index).getStatusText());
                statusTextView_pm.setText(lineStateList.get(index + 1).getStatusText());
                swap.setText("換牌");
                broadcast.setText("廣播");

                if (productLineList.get(index / 2).getSize() > 0) {
                    ProductLine productLine = productLineList.get(index / 2);
                    ProductLine tmp = new ProductLine(category, lineNum, 0);
                    cur_production.setText(productLineList.get(index / 2).getProductName(0));
                    production_serial.setOnClickListener(new DetailDialogListener(productLineList.get(index / 2)));

                    /*if (tmp.isProductionLineMatch(productLine)) {
                        swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + category + "\t" + lineNum + "<END>"));
                        swap.setText("完成");
                    } else {
                        swap.setOnClickListener(new SwapDialogListener("EXE\tSWAP\t" + productLine.getLineNum() + "<END>"));
                    }*/

                    if ((lineStateList.get(index).getStatus() == lineStateList.get(index + 1).getStatus()) && (lineStateList.get(index + 1).getStatus() == Config.GREEN)) {
                        swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + category + "\t" + lineNum + "<END>"));
                        swap.setText("完成");
                    } else if ((lineStateList.get(index).getStatus() == lineStateList.get(index + 1).getStatus()) && (lineStateList.get(index + 1).getStatus() == Config.GRAY)) {
                        swap.setOnClickListener(new SwapDialogListener("EXE\tSWAP\t" + productLine.getLineNum() + "<END>"));
                    } else {
                        swap.setEnabled(false);
                    }

                } else {
                    cur_production.setText("無生產資料");
                    production_serial.setEnabled(false);
                    swap.setEnabled(false);
                    broadcast.setEnabled(false);
                }


                name.setTextSize(Config.TEXT_SIZE);
                cur_production.setTextSize(Config.TEXT_SIZE);
                production_serial.setTextSize(Config.TEXT_SIZE);
                status_cm.setTextSize(Config.TEXT_SIZE);
                status_pm.setTextSize(Config.TEXT_SIZE);
                statusTextView_cm.setTextSize(Config.TEXT_SIZE);
                statusTextView_pm.setTextSize(Config.TEXT_SIZE);
                swap.setTextSize(Config.TEXT_SIZE);
                broadcast.setTextSize(Config.TEXT_SIZE);

                status_cm.setBackgroundColor(Config.getColor(lineStateList.get(index).getStatus()));
                status_pm.setBackgroundColor(Config.getColor(lineStateList.get(index + 1).getStatus()));


                linearLayout_status.addView(status_cm);
                linearLayout_status.addView(status_pm);
                linearLayout_statusText.addView(statusTextView_cm);
                linearLayout_statusText.addView(statusTextView_pm);

                tableRow.addView(name);
                tableRow.addView(cur_production);
                tableRow.addView(production_serial);
                tableRow.addView(linearLayout_status);
                tableRow.addView(linearLayout_statusText);
                tableRow.addView(broadcast);
                tableRow.addView(swap);

            } else {
                TextView name = new TextView(getActivity());

                name.setLayoutParams(viewParams);
                statusTextView.setGravity(Gravity.CENTER_HORIZONTAL);

                name.setText(lineStateList.get(index).getCategory() + "_" + lineStateList.get(index).getLineNum());
                if (productLineList.get(index - 8).getSize() > 0) {
                    ProductLine productLine = productLineList.get(index - 8);
                    ProductLine tmp = new ProductLine(category, lineNum, 0);

                    cur_production.setText(productLineList.get(index - 8).getProductName(0));
                    production_serial.setOnClickListener(new DetailDialogListener(productLine));

                    swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + category + "\t" + lineNum + "<END>"));

                    if (lineStateList.get(index).getStatus() == Config.GREEN) {
                        swap.setEnabled(true);
                    } else {
                        swap.setEnabled(false);
                    }

                } else {
                    cur_production.setText("無生產資料");
                    production_serial.setEnabled(false);
                    swap.setEnabled(false);
                    broadcast.setEnabled(false);
                }

                production_serial.setText("查看生產序列");
                statusTextView.setText(lineStateList.get(index).getStatusText());
                swap.setText("完成");

                name.setTextSize(Config.TEXT_SIZE);
                cur_production.setTextSize(Config.TEXT_SIZE);
                production_serial.setTextSize(Config.TEXT_SIZE);
                status.setTextSize(Config.TEXT_SIZE);
                statusTextView.setTextSize(Config.TEXT_SIZE);
                swap.setTextSize(Config.TEXT_SIZE);

                status.setBackgroundColor(Config.getColor(lineStateList.get(index).getStatus()));

                tableRow.addView(name);
                tableRow.addView(cur_production);
                tableRow.addView(production_serial);
                tableRow.addView(status);
                tableRow.addView(statusTextView);
                tableRow.addView(new TextView(getActivity()));
                tableRow.addView(swap);
            }


            tableLayout.addView(tableRow);
        }

        private void setTitleLabel() {

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
            TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            TableRow tableRow = new TableRow(getActivity());
            TextView name = new TextView(getActivity());
            TextView cur_production = new TextView(getActivity());
            TextView production_serial = new TextView(getActivity());
            TextView status = new TextView(getActivity());
            TextView statusTextView = new TextView(getActivity());
            TextView swap = new TextView(getActivity());
            TextView broadcast = new TextView(getActivity());

            tableRow.setLayoutParams(tableRowParams);
            name.setLayoutParams(viewParams);
            cur_production.setLayoutParams(viewParams);
            production_serial.setLayoutParams(viewParams);
            status.setLayoutParams(viewParams);
            statusTextView.setLayoutParams(viewParams);
            swap.setLayoutParams(viewParams);
            broadcast.setLayoutParams(viewParams);

            name.setTextColor(Color.BLACK);
            cur_production.setTextColor(Color.BLACK);
            production_serial.setTextColor(Color.BLACK);
            status.setTextColor(Color.BLACK);
            statusTextView.setTextColor(Color.BLACK);
            swap.setTextColor(Color.BLACK);
            broadcast.setTextColor(Color.BLACK);

            name.setTypeface(null, Typeface.BOLD);
            cur_production.setTypeface(null, Typeface.BOLD);
            production_serial.setTypeface(null, Typeface.BOLD);
            status.setTypeface(null, Typeface.BOLD);
            statusTextView.setTypeface(null, Typeface.BOLD);
            swap.setTypeface(null, Typeface.BOLD);
            broadcast.setTypeface(null, Typeface.BOLD);

            name.setText("產線名稱");
            cur_production.setText("目前生產");
            production_serial.setText("詳細生產序列");
            status.setText("換排燈號");
            statusTextView.setText("換排情形");
            swap.setText("換牌");
            broadcast.setText("廣播");

            name.setTextSize(Config.TEXT_TITLE_SIZE);
            cur_production.setTextSize(Config.TEXT_TITLE_SIZE);
            production_serial.setTextSize(Config.TEXT_TITLE_SIZE);
            status.setTextSize(Config.TEXT_TITLE_SIZE);
            statusTextView.setTextSize(Config.TEXT_TITLE_SIZE);
            swap.setTextSize(Config.TEXT_TITLE_SIZE);
            broadcast.setTextSize(Config.TEXT_TITLE_SIZE);

            tableRow.addView(name);
            tableRow.addView(cur_production);
            tableRow.addView(production_serial);
            tableRow.addView(status);
            tableRow.addView(statusTextView);
            tableRow.addView(broadcast);
            tableRow.addView(swap);

            tableLayout.addView(tableRow);
        }

        private ProductLine parseSwapDone(String msg) {
            String[] data = msg.split("\\t|<END>");

            return new ProductLine(data[1], data[2], 0);
        }

        private void parseUpdateMsg(String msg) {
            String[] data = msg.split("<END>");

            for (int i = 0; i < data.length; i++) {
                updateMsgQueue.add(data[i]);
            }
        }

        private void parseProductLine(String msg, boolean update) {
            String[] data = msg.split("<N>|<END>");
            final int SIZE = 3;
            int count;

            for (int i = 0; i < data.length; i++) {
                String[] detail = data[i].split("\\t");
                count = 0;
                ProductLine productLine = new ProductLine(detail[1], detail[2], (detail.length - SIZE) / 2);

                for (int j = 3; j < detail.length; j = j + 2) {
                    productLine.setProductId(detail[j], count);
                    productLine.setProductName(detail[j + 1], count);
                    productLine.setCurrent(String.valueOf(0), count);
                    productLine.setLeft(String.valueOf(0), count);
                    productLine.setTotal(String.valueOf(0), count);
                    count++;
                }

                productLineList.add(productLine);

                if (update) {

                    for (int j = 0; j < productLineList.size(); j++) {
                        ProductLine tmp = productLineList.get(j);

                        if (tmp.isProductionLineMatch(productLine)) {
                            productLineList.set(j, productLine);
                        }
                    }

                }

            }

        }

        private void parseLineState(String msg, boolean update) {
            String[] data = msg.split("\\t|<N>|<END>");

            if (update) {
                LineState lineState = new LineState(data[1], data[2], Integer.valueOf(data[3]), data[4]);

                for (int i = 0; i < lineStateList.size(); i++) {
                    LineState tmp = lineStateList.get(i);

                    if (tmp.isSwapLineMatch(lineState)) {
                        lineStateList.set(i, lineState);
                    }
                }
            } else {
                for (int i = 0; i < data.length; i = i + 5) {
                    LineState lineState = new LineState(data[i + 1], data[i + 2], Integer.valueOf(data[i + 3]), data[i + 4]);
                    lineStateList.add(lineState);
                }
            }
        }

        private class DetailDialogListener implements View.OnClickListener {

            private ProductLine productLine;

            public DetailDialogListener(ProductLine productLine) {
                this.productLine = productLine;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CPFragment.this.getActivity().getApplicationContext(), DetailDialogActivity.class);
                intent.putExtra("data", productLine);
                startActivity(intent);
            }
        }

        private class SwapDialogListener implements View.OnClickListener {
            String command;

            public SwapDialogListener(String cmd) {
                command = cmd;
            }

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("警告");
                builder.setMessage("換牌後無法取消\n你確定要執行嗎 ？");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mService.setCmd(command);
                    }
                });

                builder.show();
            }
        }

        private class SwapDoneListener implements View.OnClickListener {
            String command;

            public SwapDoneListener(String cmd) {
                command = cmd;
            }

            @Override
            public void onClick(View v) {
                publishProgress("", "");
                mService.setCmd(command);
            }
        }
    }
}
