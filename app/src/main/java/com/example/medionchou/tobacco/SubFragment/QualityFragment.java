package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.Quality;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import org.w3c.dom.Text;

/**
 * Created by Medion on 2015/11/19.
 */
public class QualityFragment extends Fragment {

    private LocalService mService;
    private QualityAsync asyncTask;
    private View view;

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
        View rootView = inflater.inflate(R.layout.frag_quality_layout, container, false);
        TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.table_layout);

        tableLayout.setStretchAllColumns(true);

        view = rootView;
        asyncTask = new QualityAsync();
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


    private class QualityAsync extends AsyncTask<Void, String, Void> {
        ProgressDialog progressDialog;
        int time[] = {R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5, R.id.time6, R.id.time7, R.id.time8, R.id.time9};
        int lineId[] = {R.id.line1, R.id.line2, R.id.line3, R.id.line4, R.id.line5, R.id.line6, R.id.line7, R.id.line8, R.id.line9};
        int productId[] = {R.id.product1, R.id.product2, R.id.product3, R.id.product4, R.id.product5, R.id.product6, R.id.product7, R.id.product8, R.id.product9};
        int weightMax[] = {R.id.weight_max1, R.id.weight_max2, R.id.weight_max3, R.id.weight_max4, R.id.weight_max5, R.id.weight_max6, R.id.weight_max7, R.id.weight_max8, R.id.weight_max9};
        int weightValue[] = {R.id.weight_value1, R.id.weight_value2, R.id.weight_value3, R.id.weight_value4, R.id.weight_value5, R.id.weight_value6, R.id.weight_value7, R.id.weight_value8, R.id.weight_value9};
        int weightMin[] = {R.id.weight_min1, R.id.weight_min2, R.id.weight_min3, R.id.weight_min4, R.id.weight_min5, R.id.weight_min6, R.id.weight_min7, R.id.weight_min8, R.id.weight_min9};
        int perimeterMax[] = {R.id.perimeter_max1, R.id.perimeter_max2, R.id.perimeter_max3, R.id.perimeter_max4, R.id.perimeter_max5, R.id.perimeter_max6, R.id.perimeter_max7, R.id.perimeter_max8, R.id.perimeter_max9};
        int perimeterValue[] = {R.id.perimeter_value1, R.id.perimeter_value2, R.id.perimeter_value3, R.id.perimeter_value4, R.id.perimeter_value5, R.id.perimeter_value6, R.id.perimeter_value7, R.id.perimeter_value8, R.id.perimeter_value9};
        int perimeterMin[] = {R.id.perimeter_min1, R.id.perimeter_min2, R.id.perimeter_min3, R.id.perimeter_min4, R.id.perimeter_min5, R.id.perimeter_min6, R.id.perimeter_min7, R.id.perimeter_min8, R.id.perimeter_min9};
        int breathMax[] = {R.id.breath_max1, R.id.breath_max2, R.id.breath_max3, R.id.breath_max4, R.id.breath_max5, R.id.breath_max6, R.id.breath_max7, R.id.breath_max8, R.id.breath_max9};
        int breathValue[] = {R.id.breath_value1, R.id.breath_value2, R.id.breath_value3, R.id.breath_value4, R.id.breath_value5, R.id.breath_value6, R.id.breath_value7, R.id.breath_value8, R.id.breath_value9};
        int breathMin[] = {R.id.breath_min1, R.id.breath_min2, R.id.breath_min3, R.id.breath_min4, R.id.breath_min5, R.id.breath_min6, R.id.breath_min7, R.id.breath_min8, R.id.breath_min9};

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
                publishProgress("CREATE");
                while (!isCancelled()) {
                    msg = mService.getUpdateMsg();

                    if (msg.length() > 0) {
                        mService.resetUpdateMsg();
                        String[] updateMsg = msg.split("<END>");

                        for (String tmp : updateMsg) {
                            if (tmp.contains("UPDATE_VALUE")) {
                                publishProgress("UPDATE", tmp);
                            }
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString() + " QualityFragment Interrupt");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (values[0].equals("CREATE")) {
                setInitialView();
            } else if (values[0].equals("UPDATE")) {
                updateView(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void updateView(String raw) {
            String[] detail = raw.split("\\t|<END>");
            Quality quality = new Quality(detail[1], detail[2], detail[3], detail[4], detail[5], detail[6], detail[7], detail[8], detail[9], detail[10], detail[11], detail[12]);
            int i = quality.getLineNum();

            TextView timeView = (TextView) view.findViewById(time[i]);
            TextView productView = (TextView) view.findViewById(productId[i]);
            TextView weightMaxView = (TextView) view.findViewById(weightMax[i]);
            TextView weightValView = (TextView) view.findViewById(weightValue[i]);
            TextView weightMinView = (TextView) view.findViewById(weightMin[i]);
            TextView perimeterMaxView = (TextView) view.findViewById(perimeterMax[i]);
            TextView perimeterValView = (TextView) view.findViewById(perimeterValue[i]);
            TextView perimeterMinView = (TextView) view.findViewById(perimeterMin[i]);
            TextView breathMaxView = (TextView) view.findViewById(breathMax[i]);
            TextView breathValView = (TextView) view.findViewById(breathValue[i]);
            TextView breathMinView = (TextView) view.findViewById(breathMin[i]);

            timeView.setText(quality.getTime());
            productView.setText(quality.getProduct());
            weightMaxView.setText(quality.getWeight()[0]);
            weightValView.setText(quality.getWeight()[1]);
            weightMinView.setText(quality.getWeight()[2]);
            perimeterMaxView.setText(quality.getPerimeter()[0]);
            perimeterValView.setText(quality.getPerimeter()[1]);
            perimeterMinView.setText(quality.getPerimeter()[2]);
            breathMaxView.setText(quality.getBreath()[0]);
            breathValView.setText(quality.getBreath()[1]);
            breathMinView.setText(quality.getBreath()[2]);


            weightValView.setTextColor(Config.getQualityColor(quality.getColorWeight()));
            perimeterValView.setTextColor(Config.getQualityColor(quality.getColorPerimeter()));
            breathValView.setTextColor(Config.getQualityColor(quality.getColorBreath()));
        }

        private void setInitialView() {
            TextView[] timeView = new TextView[9];
            TextView[] lineView = new TextView[9];
            TextView[] productView = new TextView[9];
            TextView[] weightMaxView = new TextView[9];
            TextView[] weightValView = new TextView[9];
            TextView[] weightMinView = new TextView[9];
            TextView[] perimeterMaxView = new TextView[9];
            TextView[] perimeterValView = new TextView[9];
            TextView[] perimeterMinView = new TextView[9];
            TextView[] breathMaxView = new TextView[9];
            TextView[] breathValView = new TextView[9];
            TextView[] breathMinView = new TextView[9];
            TextView weight = (TextView) view.findViewById(R.id.weight);
            TextView perimeter = (TextView) view.findViewById(R.id.perimeter);
            TextView breathLabel = (TextView) view.findViewById(R.id.breath);

            weight.setText("重量");
            perimeter.setText("圓周");
            breathLabel.setText("透氣率");

            weight.setTextSize(Config.TEXT_TITLE_SIZE);
            perimeter.setTextSize(Config.TEXT_TITLE_SIZE);
            breathLabel.setTextSize(Config.TEXT_TITLE_SIZE);

            weight.setTextColor(Color.BLACK);
            perimeter.setTextColor(Color.BLACK);
            breathLabel.setTextColor(Color.BLACK);

            weight.setTypeface(null, Typeface.BOLD);
            perimeter.setTypeface(null, Typeface.BOLD);
            breathLabel.setTypeface(null, Typeface.BOLD);


            for (int i = 0; i < 9; i++) {
                timeView[i] = (TextView) view.findViewById(time[i]);
                lineView[i] = (TextView) view.findViewById(lineId[i]);
                productView[i] = (TextView) view.findViewById(productId[i]);
                weightMaxView[i] = (TextView) view.findViewById(weightMax[i]);
                weightValView[i] = (TextView) view.findViewById(weightValue[i]);
                weightMinView[i] = (TextView) view.findViewById(weightMin[i]);
                perimeterMaxView[i] = (TextView) view.findViewById(perimeterMax[i]);
                perimeterValView[i] = (TextView) view.findViewById(perimeterValue[i]);
                perimeterMinView[i] = (TextView) view.findViewById(perimeterMin[i]);
                breathMaxView[i] = (TextView) view.findViewById(breathMax[i]);
                breathValView[i] = (TextView) view.findViewById(breathValue[i]);
                breathMinView[i] = (TextView) view.findViewById(breathMin[i]);


                timeView[i].setText("00:00:00");
                lineView[i].setText(String.valueOf(i+1));
                productView[i].setText("無");
                weightMaxView[i].setText("0.00");
                weightValView[i].setText("0.00");
                weightMinView[i].setText("0.00");
                perimeterMaxView[i].setText("0.00");
                perimeterValView[i].setText("0.00");
                perimeterMinView[i].setText("0.00");
                breathMaxView[i].setText("0.00");
                breathValView[i].setText("0.00");
                breathMinView[i].setText("0.00");

                timeView[i].setTextSize(Config.TEXT_SIZE);
                lineView[i].setTextSize(Config.TEXT_SIZE);
                productView[i].setTextSize(Config.TEXT_SIZE);
                weightMaxView[i].setTextSize(Config.TEXT_SIZE);
                weightValView[i].setTextSize(Config.TEXT_SIZE);
                weightMinView[i].setTextSize(Config.TEXT_SIZE);
                perimeterMaxView[i].setTextSize(Config.TEXT_SIZE);
                perimeterValView[i].setTextSize(Config.TEXT_SIZE);
                perimeterMinView[i].setTextSize(Config.TEXT_SIZE);
                breathMaxView[i].setTextSize(Config.TEXT_SIZE);
                breathValView[i].setTextSize(Config.TEXT_SIZE);
                breathMinView[i].setTextSize(Config.TEXT_SIZE);

            }

        }
    }

}
