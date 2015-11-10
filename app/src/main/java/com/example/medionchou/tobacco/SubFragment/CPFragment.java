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
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Activity.LoggedInActivity;
import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.LineState;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.Activity.DetailDialogActivity;
import com.example.medionchou.tobacco.DataContainer.RecipeList;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private final int NUM_PRODUCTION_LINE = 9;


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
        private List<RecipeList> recipeLists = new ArrayList<>();

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

                sendCommand(Command.PRODUCT);
                sendCommand(Command.SWAP);
                sendCommand(Command.RECIPE_LIST);

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
                                if (!text.contains("SWAP_HISTORY"))
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
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString());
            }
            return (Void) null;
        }

        private void sendCommand(String cmd) {
            String msg = "";
            try {
                while (msg.length() == 0) {
                    mService.setCmd(cmd);
                    Thread.sleep(1000);
                    msg = mService.getQueryReply();
                }
                switch (cmd) {
                    case Command.PRODUCT:
                        parseProductLine(msg, false);
                        break;
                    case Command.SWAP:
                        parseLineState(msg, false);
                        break;
                    case Command.RECIPE_LIST:
                        parseRecipeList(msg);
                        break;
                }
                mService.resetQueryReply();
            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString() + "SendCommand thread interrupted");
            }
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

                if (i < NUM_PRODUCTION_LINE*2) {
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
            TextView name = new TextView(getActivity());
            TextView cur_production = new TextView(getActivity());
            Button production_serial = new Button(getActivity());
            TextView status = new TextView(getActivity());
            TextView statusTextView = new TextView(getActivity());
            Button swap = new Button(getActivity());
            Button broadcast = new Button(getActivity());

            if (index == 0) {
                setTitleLabel();
            }

            tableRowParams.setMargins(0, 20, 0, 0);
            viewParams_topMargin.setMargins(0, 10, 0, 0);

            tableRow.setLayoutParams(tableRowParams);
            name.setLayoutParams(viewParams);
            cur_production.setLayoutParams(viewParams);
            production_serial.setLayoutParams(viewParams);
            status.setLayoutParams(viewParams);
            statusTextView.setLayoutParams(viewParams);
            swap.setLayoutParams(viewParams);
            broadcast.setLayoutParams(viewParams);

            status.setGravity(Gravity.CENTER_HORIZONTAL);
            statusTextView.setGravity(Gravity.CENTER_HORIZONTAL);

            name.setTextSize(Config.TEXT_SIZE);
            cur_production.setTextSize(Config.TEXT_SIZE);
            production_serial.setTextSize(Config.TEXT_SIZE);
            status.setTextSize(Config.TEXT_SIZE);
            status.setTextSize(Config.TEXT_SIZE);
            statusTextView.setTextSize(Config.TEXT_SIZE);
            swap.setTextSize(Config.TEXT_SIZE);
            broadcast.setTextSize(Config.TEXT_SIZE);

            cur_production.setMaxEms(5);

            if (index < NUM_PRODUCTION_LINE*2) {

                name.setText("生產線_" + String.valueOf(index / 2 + 1));
                production_serial.setText("查看生產序列");
                status.setText(lineStateList.get(index).getCategory());//
                statusTextView.setText(lineStateList.get(index).getStatusText());//
                swap.setText("換牌");
                broadcast.setText("廣播");

                if (productLineList.get(index / 2).getSize() > 0) {
                    ProductLine productLine = productLineList.get(index / 2);
                    cur_production.setText(productLine.getProductName(0));
                    production_serial.setOnClickListener(new DetailDialogListener(productLine));

                    if (lineStateList.get(index).getStatus() == Config.GREEN) {
                        swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + category + "\t" + lineNum + "<END>"));
                        swap.setText("完成");
                    } else if (lineStateList.get(index).getStatus() == Config.GRAY && lineStateList.get(index + 1).getStatus() == Config.GRAY) {
                        swap.setOnClickListener(new SwapDialogListener("EXE\tSWAP\t" + productLine.getLineNum() + "<END>", productLine));
                    } else {
                        swap.setEnabled(false);
                    }

                    broadcast.setOnClickListener(new BroadcastListener(productLine.getCategory() + "\t" + productLine.getLineNum()));

                } else {
                    cur_production.setText("無生產資料");
                    production_serial.setEnabled(false);
                    swap.setEnabled(false);
                    /*ProductLine productLine = productLineList.get(index / 2);
                    swap.setOnClickListener(new SwapDialogListener("EXE\tSWAP\t" + productLine.getLineNum() + "<END>", productLine.getProductName(0)));*/
                    broadcast.setEnabled(false);
                }

                if (lineStateList.get(index).getStatus() == Config.GREEN) {
                    String doneCategory = productLineList.get(index / 2).getCategory();
                    String doneLineNUm = productLineList.get(index / 2).getLineNum();
                    swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + doneCategory + "\t" + doneLineNUm + "<END>"));
                    swap.setText("完成");
                    swap.setEnabled(true);
                }

                status.setBackgroundColor(Config.getColor(lineStateList.get(index).getStatus()));

            } else {
                name.setText(lineStateList.get(index).getCategory() + "_" + lineStateList.get(index).getLineNum());
                if (productLineList.get(index - NUM_PRODUCTION_LINE).getSize() > 0) {
                    ProductLine productLine = productLineList.get(index - NUM_PRODUCTION_LINE);

                    cur_production.setText(productLine.getProductName(0));
                    production_serial.setOnClickListener(new DetailDialogListener(productLine));

                    swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + category + "\t" + lineNum + "<END>"));

                    if (lineStateList.get(index).getStatus() == Config.GREEN) {
                        swap.setEnabled(true);
                    } else {
                        swap.setEnabled(false);
                    }

                    broadcast.setOnClickListener(new BroadcastListener(productLine.getCategory() + "\t" + productLine.getLineNum()));

                } else {
                    cur_production.setText("無生產資料");
                    production_serial.setEnabled(false);
                    swap.setEnabled(false);
                    broadcast.setEnabled(false);
                }

                if (lineStateList.get(index).getStatus() == Config.GREEN) {
                    String doneCategory = productLineList.get(index - NUM_PRODUCTION_LINE).getCategory();
                    String doneLineNum = productLineList.get(index - NUM_PRODUCTION_LINE).getLineNum();
                    swap.setOnClickListener(new SwapDoneListener("SWAP_DONE_RECEIVE\t" + doneCategory + "\t" + doneLineNum + "<END>"));
                    swap.setEnabled(true);
                }

                production_serial.setText("查看生產序列");
                statusTextView.setText(lineStateList.get(index).getStatusText());
                swap.setText("完成");
                broadcast.setText("廣播");

                status.setBackgroundColor(Config.getColor(lineStateList.get(index).getStatus()));
            }

            tableRow.addView(name);
            tableRow.addView(cur_production);
            tableRow.addView(production_serial);
            tableRow.addView(status);
            tableRow.addView(statusTextView);
            tableRow.addView(broadcast);
            tableRow.addView(swap);
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
                String[] detail = data[i].split("\\t", -1);
                count = 0;
                ProductLine productLine = new ProductLine(detail[1], detail[2], (detail.length - SIZE) / 3);

                for (int j = 3; j < detail.length; j = j + 3) {
                    productLine.setProductId(detail[j], count);
                    productLine.setProductName(detail[j + 1], count);
                    productLine.setCurrent(String.valueOf(0), count);
                    productLine.setLeft(String.valueOf(0), count);
                    productLine.setTotal(detail[j + 2], count);
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

        private void parseRecipeList(String rawData) {
            String[] data = rawData.split("<N>|<END>");

            for (int i = 0; i < data.length; i++) {
                String[] detailRecipe = data[i].split("\\t");
                RecipeList recipeList = new RecipeList(detailRecipe[1], detailRecipe[2]);
                recipeLists.add(recipeList);
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

        private class BroadcastListener implements View.OnClickListener {
            String lineInfo;

            public BroadcastListener(String msg) {
                lineInfo = msg;
            }

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View customView = inflater.inflate(R.layout.dialog_broadcast, null);
                EditText broadcastMsg;
                Calendar cal = Calendar.getInstance();
                DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                String date = dateFormat.format(cal.getTime());
                builder.setTitle("請輸入廣播訊息");
                builder.setView(customView);
                broadcastMsg = (EditText) customView.findViewById(R.id.broadcast_text);
                broadcastMsg.setText("即將在 " + date + " 進行換牌，請注意。");

                builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String broadcastMsg = ((EditText) customView.findViewById(R.id.broadcast_text)).getText().toString();
                        String cmd = "EXE\tBROADCAST\t" + lineInfo + "\t" + broadcastMsg + "<END>";

                        mService.setCmd(cmd);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            }
        }

        private class SwapDialogListener implements View.OnClickListener {
            private String command;
            private List<String> tmp = new ArrayList<>();
            private String selectedItem;
            private ProductLine productLine;

            public SwapDialogListener(String cmd, ProductLine productLine) {
                command = cmd;
                this.productLine = productLine;
                tmp.add("請選擇物料");
                for (int i = 0; i < recipeLists.size(); i++)
                    tmp.add(recipeLists.get(i).getProductName());
            }

            @Override
            public void onClick(View v) {
                final View custom = getActivity().getLayoutInflater().inflate(R.layout.swap_confirm_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                Spinner spinner = (Spinner) custom.findViewById(R.id.spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, tmp);
                ItemSelectedListener listener = new ItemSelectedListener(this);
                final int index;

                adapter.setDropDownViewResource(R.layout.spinner_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(listener);

                builder.setView(custom);
                builder.setTitle("警告");

                if (productLine.getSize() == 1) {
                    builder.setMessage(Html.fromHtml("即將清除<font color='red'>" + productLine.getProductName(0) + "</font>\n你確定要執行嗎 ？"));
                    index = 0;
                } else {
                    builder.setMessage(Html.fromHtml("即將更換成<font color='red'>" + productLine.getProductName(1) + "</font>\n你確定要執行嗎 ？"));
                    index = 1;
                }

                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String confirmedId = ((EditText) custom.findViewById(R.id.confirm_edit_text)).getText().toString();
                        String workerId = ((LoggedInActivity) getActivity()).getWorkerId();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

//                        if (workerId.equals(confirmedId)) {
//                            if (selectedItem.equals(productLine.getProductName(index))) {
//                                mService.setCmd(command);
//                            } else {
//                                builder.setTitle("警告");
//                                builder.setMessage("選擇的物料必須與換排物料相同");
//                                builder.show();
//                            }
//                        } else {
//                            builder.setTitle("警告");
//                            builder.setMessage("輸入編號必須與登入編號相同");
//                            builder.show();
//                        }
                        mService.setCmd(command);
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                alert.getWindow().getAttributes();

                ((TextView) alert.findViewById(android.R.id.message)).setTextSize(30);

            }

            public void setSelectedItem(String item) {
                selectedItem = item;
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

        private class ItemSelectedListener implements Spinner.OnItemSelectedListener {
            private SwapDialogListener  listener;
            public ItemSelectedListener(SwapDialogListener tmp) {
                listener = tmp;
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.setSelectedItem(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }
    }
}
