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
import android.widget.TableLayout;
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
        int boxLabelId[] = {R.id.box_label1, R.id.box_label2, R.id.box_label3, R.id.box_label4, R.id.box_label5, R.id.box_label6, R.id.box_label7, R.id.box_label8, R.id.box_label9};

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
                TextView recentBox = (TextView) getActivity().findViewById(R.id.recentTextView);
                String boxRecent =  mService.getRecentBox();
                boxRecent = boxRecent.replace("BOX_RECENT\t", "");
                boxRecent = boxRecent.replace("<END>", "");

                recentBox.setText(boxRecent);
                recentBox.setTextSize(Config.TEXT_SIZE);

            }

            if (values[1].equals("BOX")) {
                updateBoxes(values[2]);
            }

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

        private void createProductView() {
            TextView[] lineNum = new TextView[9];
            TextView[] product = new TextView[9];
            TextView[] boxNum = new TextView[9];
            TextView[] boxLabel = new TextView[9];

            for (int i = 0; i < lineId.length; i++) {
                ProductLine productLine = productLineList.get(i);
                String tmp;
                lineNum[i] = (TextView)tableLayout.findViewById(lineId[i]);
                product[i] = (TextView)tableLayout.findViewById(productId[i]);
                boxNum[i] = (TextView)tableLayout.findViewById(boxNumId[i]);
                boxLabel[i] = (TextView)tableLayout.findViewById(boxLabelId[i]);

                tmp = productLine.getSize() > 0 ? productLine.getProductName(0) : "(無)";

                lineNum[i].setText(String.valueOf(i+1));
                product[i].setText(tmp);
                boxLabel[i].setText("箱");

                if (boxNum[i].getText().toString().equals("")) {
                    boxNum[i].setText("0");
                }

                lineNum[i].setTextSize(Config.TEXT_SIZE);
                product[i].setTextSize(Config.TEXT_SIZE);
                boxNum[i].setTextSize(Config.TEXT_SIZE);
                boxLabel[i].setTextSize(Config.TEXT_SIZE);

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
            TextView boxes = (TextView) tableLayout.findViewById(boxNumId[index]);

            boxes.setText(detail[2]);
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
