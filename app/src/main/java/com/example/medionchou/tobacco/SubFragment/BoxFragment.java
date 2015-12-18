package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Medion on 2015/11/19.
 */
public class BoxFragment extends Fragment {

    private LocalService mService;
    private BoxAsyncTask asyncTask;
    private TableLayout tableLayout;
    private TableLayout parentLayout;
    private TextView totalBox;

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_current_box_production_layout, container, false);
        tableLayout = (TableLayout) rootView.findViewById(R.id.table_layout);
        tableLayout.setStretchAllColumns(true);

        parentLayout = (TableLayout) rootView.findViewById(R.id.box_layout);
        parentLayout.setStretchAllColumns(true);

        totalBox = (TextView) rootView.findViewById(R.id.total_box);

        asyncTask = new BoxAsyncTask();
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

    private class BoxAsyncTask extends AsyncTask<Void, String, Void> {
        ProgressDialog progressDialog;
        List<ProductLine> productLineList = new ArrayList<>();
        int lineId[] = {R.id.line1, R.id.line2, R.id.line3, R.id.line4, R.id.line5, R.id.line6, R.id.line7, R.id.line8, R.id.line9};
        int productId[] = {R.id.product1, R.id.product2, R.id.product3, R.id.product4, R.id.product5, R.id.product6, R.id.product7, R.id.product8, R.id.product9};
        int boxNumId[] = {R.id.box_num1, R.id.box_num2, R.id.box_num3, R.id.box_num4, R.id.box_num5, R.id.box_num6, R.id.box_num7, R.id.box_num8, R.id.box_num9};

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
            String msg = "";
            try {
                sendCommand(Command.PRODUCT);

                publishProgress("PRODUCT", "");
                publishProgress("BOX_RECENT", "");

                while (!isCancelled()) {
                    msg = mService.getUpdateMsg();

                    if (msg.length() > 0) {
                        mService.resetUpdateMsg();

                        String[] updateMsg = msg.split("<END>");
                        boolean isUpdate = false;

                        for (String tmp : updateMsg) {
                            if (tmp.contains("PRODUCT")) {
                                parseProductLine(tmp, true);
                                isUpdate = true;
                            } else if (tmp.contains("UPDATE_BOX")) {
                                if (!tmp.contains("UPDATE_BOX_HISTORY"))
                                    publishProgress("", "BOX", tmp);
                            }
                        }

                        if (isUpdate)
                            publishProgress("PRODUCT", "");
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString() + " BoxFragment Interrupted");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("PRODUCT")) {
                createProductView();
            }

            if (values[0].equals("BOX_RECENT")) {
                String raw = mService.getRecentBox();
                createHistoryBoxView(raw);
            }

            if (values[1].equals("BOX")) {
                updateBoxes(values[2]);
            }

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

        private void createHistoryBoxView(String raw) {
            String[] data = raw.split("\\t|<END>");

            for (int i = 0; i < data.length;) {
                if (i == 0) {
                    Log.v("MyLog", data[i] + data[i+1] + data[i+2] + "總箱數");
                    drawView(data[i + 1], data[i + 2], data[i + 3], data[i + 4], data[i + 5], Html.fromHtml("小計"), i);
                    i = i + 6;
                } else {
                    if (i == 54) {
                        Log.v("MyLog", data[i] + data[i + 1] + data[i + 2] + Html.fromHtml(data[i + 3] + "\r\n<font color='yellow'>" + data[i + 4] + "</font>"));
                        //drawView(data[i], data[i + 1], data[i + 2], data[i + 3], data[i + 4], Html.fromHtml(data[i + 5] + "<br><font color='green'>" + data[i + 6] + "</font>"), i);
                        drawView(data[i], data[i + 1], data[i + 2], data[i + 3], data[i + 4], Html.fromHtml(data[i + 5]), i);

                        totalBox.setText("總箱數: "+data[i+6]);
                        i = data.length;
                    } else {
                        drawView(data[i], data[i + 1], data[i + 2], data[i + 3], data[i + 4], Html.fromHtml(data[i + 5]), i);
                        i  = i + 6;
                    }
                }
            }
        }

        private void drawView(String col, String col1, String col2, String col3, String col4, Spanned col5, int row) {

            TableRow.LayoutParams rowParam = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
            TableLayout.LayoutParams param;


            TableRow layout = new TableRow(getActivity());
            TextView colView = new TextView(getActivity());
            TextView col1View = new TextView(getActivity());
            TextView col2View = new TextView(getActivity());
            TextView col3View = new TextView(getActivity());
            TextView col4View = new TextView(getActivity());
            TextView col5View = new TextView(getActivity());

            if (row == 0) {
                param = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                layout.setBackgroundColor(getResources().getColor(R.color.yellow));
            }
            else
                param = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);


            layout.setLayoutParams(param);

            colView.setLayoutParams(rowParam);
            col1View.setLayoutParams(rowParam);
            col2View.setLayoutParams(rowParam);
            col3View.setLayoutParams(rowParam);
            col4View.setLayoutParams(rowParam);
            col5View.setLayoutParams(rowParam);

            col2 = col2.replace("\r", "");

            colView.setText(col);
            col1View.setText(col1);
            col2View.setText(col2);
            col3View.setText(col3);
            col4View.setText(col4);
            col5View.setText(col5);

            colView.setTextSize(Config.TEXT_SIZE);
            col1View.setTextSize(Config.TEXT_SIZE);
            col2View.setTextSize(Config.TEXT_SIZE);
            col3View.setTextSize(Config.TEXT_SIZE);
            col4View.setTextSize(Config.TEXT_SIZE);
            col5View.setTextSize(Config.TEXT_SIZE);

            colView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            col1View.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            col2View.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            col3View.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            col4View.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            col5View.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

            if (row == 0) {
                colView.setTextColor(getResources().getColor(R.color.red));
                col1View.setTextColor(getResources().getColor(R.color.red));
                col2View.setTextColor(getResources().getColor(R.color.red));
                col3View.setTextColor(getResources().getColor(R.color.red));
                col4View.setTextColor(getResources().getColor(R.color.red));
                col5View.setTextColor(getResources().getColor(R.color.red));
            } else {
                colView.setTextColor(Color.BLACK);
                col1View.setTextColor(Color.BLACK);
                col2View.setTextColor(Color.BLACK);
                col3View.setTextColor(Color.BLACK);
                col4View.setTextColor(Color.BLACK);
                col5View.setTextColor(Color.BLACK);

                col5View.setTextSize(40);
            }

            layout.addView(colView);
            layout.addView(col1View);
            layout.addView(col2View);
            layout.addView(col3View);
            layout.addView(col4View);
            layout.addView(col5View);

            parentLayout.addView(layout);
        }

        private void createProductView() {
            TextView[] lineNum = new TextView[9];
            TextView[] product = new TextView[9];
            TextView[] boxNum = new TextView[9];

            for (int i = 0; i < lineId.length; i++) {
                ProductLine productLine = productLineList.get(i);
                String tmp;
                lineNum[i] = (TextView)tableLayout.findViewById(lineId[i]);
                product[i] = (TextView)tableLayout.findViewById(productId[i]);
                boxNum[i] = (TextView)tableLayout.findViewById(boxNumId[i]);

                tmp = productLine.getSize() > 0 ? productLine.getProductName(0) : "(無)";

                lineNum[i].setText(String.valueOf(i + 1) + " 號機");
                product[i].setText(tmp);

                if (boxNum[i].getText().toString().equals("")) {
                    boxNum[i].setText("0 / 0");
                }

                lineNum[i].setTextSize(40);
                product[i].setTextSize(Config.TEXT_SIZE);
                boxNum[i].setTextSize(Config.TEXT_SIZE);

                product[i].setMaxEms(3);
            }
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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

        private void updateBoxes(String msg) {
            String[] detail = msg.split("\\t|<END>");
            int index = Integer.valueOf(detail[1]) - 1;
            TextView boxNum = (TextView)tableLayout.findViewById(boxNumId[index]);

            boxNum.setText(detail[2] + " / " + detail[3]);
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
                }
                mService.resetQueryReply();
            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString() + "SendCommand thread interrupted");
            }
        }


    }
}
