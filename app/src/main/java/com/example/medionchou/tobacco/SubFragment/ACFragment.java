package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.Recipe;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Medion on 2015/9/3.
 */
public class ACFragment extends Fragment {

    private LocalService mService;
    private TableLayout tableLayout;
    private RecipeAsyncTask asyncTask;

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
        View rootView = inflater.inflate(R.layout.frag_recipe_layout, container, false);

        tableLayout = (TableLayout) rootView.findViewById(R.id.recipe_table_layout);
        tableLayout.setStretchAllColumns(true);

        asyncTask = new RecipeAsyncTask();
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

    private class RecipeAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        List<Recipe> recipeList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.getting_online_state));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String msg = "";
            try {

                mService.setCmd(Command.RECIPE_NOW);
                Thread.sleep(2000);

                while (msg.length() == 0) {
                    msg = mService.getQueryReply();
                    Thread.sleep(1000);
                }

                parseRecipeMsg(msg, false);

                mService.resetQueryReply();

                publishProgress((Void) null);

                while (!isCancelled()) {
                    msg = mService.getUpdateMsg();

                    if (msg.length() > 0) {
                        String[] updatInfo = msg.split("<END>");

                        for (String tmp : updatInfo) {
                            if (tmp.contains("UPDATE_RECIPE_NOW")) {
                                parseRecipeMsg(msg, true);
                                publishProgress((Void) null);
                            }
                        }
                        mService.resetUpdateMsg();
                    }
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                Log.e("MyLog", e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            updateGui();
        }

        private void parseRecipeMsg(String recipeMsg, boolean update) {

            String[] data = recipeMsg.split("\\t|<N>|<END>");

            if (update) {
                Recipe recipe = new Recipe(data[1], data[2], data[3]);

                for (int i = 0; i < recipeList.size(); i++) {
                    Recipe tmp = recipeList.get(i);
                    if (recipe.isBucketMatch(tmp)) {
                        recipeList.set(i, recipe);
                    }
                }

            } else {
                for (int i = 0; i < data.length; i = i + 4) {
                    Recipe recipe = new Recipe(data[i + 1], data[i + 2], data[i + 3]);
                    recipeList.add(recipe);
                }
            }

        }

        private void updateGui() {
            tableLayout.removeAllViews();

            for (int i = 0; i < recipeList.size(); i++) {
                Recipe recipe = recipeList.get(i);
                inflateTextView(recipe, i);
            }
        }

        private void inflateTextView(Recipe recipe, int indexOfTitle) {

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
            TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow tableRow = new TableRow(getActivity());
            TextView bucketTextView = new TextView(getActivity());
            TextView recipeIdTextView = new TextView(getActivity());
            TextView recipeNameTextView = new TextView(getActivity());


            tableRow.setLayoutParams(tableRowParams);
            bucketTextView.setLayoutParams(textViewParams);
            recipeIdTextView.setLayoutParams(textViewParams);
            recipeNameTextView.setLayoutParams(textViewParams);

            bucketTextView.setText(recipe.getBucketNum());
            recipeIdTextView.setText(recipe.getRecipeId());
            recipeNameTextView.setText(recipe.getRecipeName());

            bucketTextView.setTextSize(Config.TEXT_SIZE);
            recipeIdTextView.setTextSize(Config.TEXT_SIZE);
            recipeNameTextView.setTextSize(Config.TEXT_SIZE);

            recipeIdTextView.setMaxEms(5);

            if (indexOfTitle == 0) {
                TableRow titleRow = new TableRow(getActivity());
                TextView bucketTitle = new TextView(getActivity());
                TextView recipeIdTitle = new TextView(getActivity());
                TextView recipeNameTitle = new TextView(getActivity());

                titleRow.setLayoutParams(tableRowParams);
                bucketTitle.setLayoutParams(textViewParams);
                recipeIdTitle.setLayoutParams(textViewParams);
                recipeNameTitle.setLayoutParams(textViewParams);

                bucketTitle.setText("桶號");
                recipeIdTitle.setText("配方編號");
                recipeNameTitle.setText("配方名稱");

                bucketTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                recipeIdTitle.setTextSize(Config.TEXT_TITLE_SIZE);
                recipeNameTitle.setTextSize(Config.TEXT_TITLE_SIZE);

                bucketTitle.setTextColor(Color.BLACK);
                recipeIdTitle.setTextColor(Color.BLACK);
                recipeNameTitle.setTextColor(Color.BLACK);

                bucketTitle.setTypeface(null, Typeface.BOLD);
                recipeIdTitle.setTypeface(null, Typeface.BOLD);
                recipeNameTitle.setTypeface(null, Typeface.BOLD);

                titleRow.addView(bucketTitle);
                titleRow.addView(recipeNameTitle);
                titleRow.addView(recipeIdTitle);

                tableLayout.addView(titleRow);
            }

            tableRow.addView(bucketTextView);
            tableRow.addView(recipeIdTextView);
            tableRow.addView(recipeNameTextView);

            tableLayout.addView(tableRow);
        }
    }

}
