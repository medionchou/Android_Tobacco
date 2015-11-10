package com.example.medionchou.tobacco.SubFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.DataContainer.RecipeList;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Medion on 2015/11/10.
 */
public class InsertFragment extends Fragment {

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
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        asyncTask = new ProductAsynTask();
        asyncTask.execute((Void) null);

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
        private List<RecipeList> recipeLists = new ArrayList<>();


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
            String msg;

            try {
                sendCommand(Command.PRODUCT);
                sendCommand(Command.RECIPE_LIST);

                publishProgress("", "");


                while (!isCancelled()) {
                    msg = mService.getUpdateMsg();

                    if (msg.length() > 0) {
                        String[] updateInfo = msg.split("<END>");
                        mService.resetUpdateMsg();

                        for (String tmp : updateInfo) {

                            if (tmp.contains("UPDATE_PRODUCT")) {
                                parseProductLine(tmp, true);
                            }
                        }

                        publishProgress("", "");
                    }

                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Log.v("MyLog", "InsertFragment " + e.toString());
            }
            return (Void)null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            inflateView();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
                    case Command.RECIPE_LIST:
                        parseRecipeList(msg);
                        break;
                }
                mService.resetQueryReply();
            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString() + "SendCommand thread interrupted");
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

        private void parseRecipeList(String rawData) {
            String[] data = rawData.split("<N>|<END>");

            for (int i = 0; i < data.length; i++) {
                String[] detailRecipe = data[i].split("\\t");
                RecipeList recipeList = new RecipeList(detailRecipe[1], detailRecipe[2]);
                recipeLists.add(recipeList);
            }
        }

        private void inflateView() {
            tableLayout.removeAllViews();
            addTableTitle();

            for (int i = 0; i < 9; i++) {
                addTableRow(i);
            }
        }

        private void addTableRow(int i) {
            TableRow tableRow = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.production_row_item, null);
            TextView lineNum = (TextView)tableRow.findViewById(R.id.line_num);
            TextView cur_production = (TextView)tableRow.findViewById(R.id.current_production);
            TextView next_production = (TextView)tableRow.findViewById(R.id.next_production);
            Button insert = (Button)tableRow.findViewById(R.id.insert);

            ProductLine productLine = productLineList.get(i);

            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1));

            lineNum.setTextSize(Config.TEXT_SIZE);
            cur_production.setTextSize(Config.TEXT_SIZE);
            next_production.setTextSize(Config.TEXT_SIZE);
            insert.setTextSize(Config.TEXT_SIZE);

            cur_production.setMaxEms(5);
            next_production.setMaxEms(5);

            insert.setEnabled(false);

            lineNum.setText("生產線_" + productLine.getLineNum());
            if (productLine.getSize() >= 2) {
                cur_production.setText(productLine.getProductName(0));
                next_production.setText(productLine.getProductName(1));
                insert.setEnabled(true);
            } else if (productLine.getSize() == 1) {
                cur_production.setText(productLine.getProductName(0));
                next_production.setText("無生產資料");

            } else {
                cur_production.setText("無生產資料");
                next_production.setText("無生產資料");
            }
            insert.setText("修改");
            insert.setOnClickListener(new ModificationListener(productLine));

            tableLayout.addView(tableRow);
        }

        private void addTableTitle() {
            TableRow tableRow = new TableRow(getActivity());
            TextView lineNum = new TextView(getActivity());
            TextView cur_production = new TextView(getActivity());
            TextView next_production = new TextView(getActivity());
            TextView insert = new TextView(getActivity());

            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            lineNum.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            cur_production.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            next_production.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            insert.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            lineNum.setText("產線名稱");
            cur_production.setText("目前生產");
            next_production.setText("下次生產");
            insert.setText("修改");

            lineNum.setTextSize(Config.TEXT_TITLE_SIZE);
            cur_production.setTextSize(Config.TEXT_TITLE_SIZE);
            next_production.setTextSize(Config.TEXT_TITLE_SIZE);
            insert.setTextSize(Config.TEXT_TITLE_SIZE);

            lineNum.setTypeface(null, Typeface.BOLD);
            cur_production.setTypeface(null, Typeface.BOLD);
            next_production.setTypeface(null, Typeface.BOLD);
            insert.setTypeface(null, Typeface.BOLD);

            lineNum.setTextColor(Color.BLACK);
            cur_production.setTextColor(Color.BLACK);
            next_production.setTextColor(Color.BLACK);
            insert.setTextColor(Color.BLACK);

            tableRow.addView(lineNum);
            tableRow.addView(cur_production);
            tableRow.addView(next_production);
            tableRow.addView(insert);

            tableLayout.addView(tableRow);
        }

        private class ModificationListener implements View.OnClickListener {
            ProductLine productLine;

            public ModificationListener(ProductLine productLine) {
                this.productLine = productLine;
            }

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setView(getCustomLayout());

                builder.setTitle("Testtt");

                builder.show();
            }

            private ScrollView getCustomLayout() {
                ScrollView scrollView = new ScrollView(getActivity());
                TableLayout tableLayout = new TableLayout(getActivity());

                scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));
                tableLayout.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));

                tableLayout.setStretchAllColumns(true);

                tableLayout.addView(getTableTitle());

                for (int i = 1; i < productLine.getSize(); i++) {
                   TableRow row = getTableRow(i);

                    tableLayout.addView(row);
                }

                scrollView.addView(tableLayout);

                return scrollView;
            }

            private TableRow getTableTitle() {
                TableRow tableRow = new TableRow(getActivity());
                TextView identity = new TextView(getActivity());
                TextView productName = new TextView(getActivity());
                TextView quantity = new TextView(getActivity());
                TextView delete = new TextView(getActivity());
                TextView modify = new TextView(getActivity());

                tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

                identity.setText("編號");
                productName.setText("名稱");
                quantity.setText("數量");
                delete.setText("刪除");
                modify.setText("插入");

                identity.setTextSize(Config.DIALOG_TITLE_SIZE);
                productName.setTextSize(Config.DIALOG_TITLE_SIZE);
                quantity.setTextSize(Config.DIALOG_TITLE_SIZE);
                delete.setTextSize(Config.DIALOG_TITLE_SIZE);
                modify.setTextSize(Config.DIALOG_TITLE_SIZE);

                identity.setTypeface(null, Typeface.BOLD);
                productName.setTypeface(null, Typeface.BOLD);
                quantity.setTypeface(null, Typeface.BOLD);
                delete.setTypeface(null, Typeface.BOLD);
                modify.setTypeface(null, Typeface.BOLD);

                identity.setTextColor(Color.BLACK);
                productName.setTextColor(Color.BLACK);
                quantity.setTextColor(Color.BLACK);
                delete.setTextColor(Color.BLACK);
                modify.setTextColor(Color.BLACK);

                tableRow.addView(identity);
                tableRow.addView(productName);
                tableRow.addView(quantity);
                tableRow.addView(delete);
                tableRow.addView(modify);

                return tableRow;
            }

            private TableRow getTableRow(int i) {
                TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.dialog_item, null);
                TextView identity = (TextView)tableRow.findViewById(R.id.identifier);
                TextView productName = (TextView)tableRow.findViewById(R.id.product_name);
                TextView quantity = (TextView)tableRow.findViewById(R.id.quantity);
                TextView delete = (TextView)tableRow.findViewById(R.id.delete);
                TextView modify = (TextView)tableRow.findViewById(R.id.modify);

                identity.setText(productLine.getProductId(i));
                productName.setText(productLine.getProductName(i));
                quantity.setText(productLine.getTotal(i));
                delete.setText("刪除");
                modify.setText("插入");

                identity.setTextSize(Config.DIALOG_SIZE);
                productName.setTextSize(Config.DIALOG_SIZE);
                quantity.setTextSize(Config.DIALOG_SIZE);
                delete.setTextSize(Config.DIALOG_SIZE);
                modify.setTextSize(Config.DIALOG_SIZE);

                return tableRow;
            }
        }
    }
}
