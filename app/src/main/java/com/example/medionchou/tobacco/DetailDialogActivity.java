package com.example.medionchou.tobacco;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.R;

public class DetailDialogActivity extends Activity {

    private ProductLine productLine;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detail_dialog);
        tableLayout = (TableLayout) findViewById(R.id.detail_table_layout);
        tableLayout.setStretchAllColumns(true);
        Bundle bundle = getIntent().getExtras();
        productLine = bundle.getParcelable("data");

        updateGui();

    }

    private void updateGui() {


        for (int i = 0; i < productLine.getSize(); i++) {
            inflateView(i);
        }
    }

    private void inflateView(int index) {
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
        TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow tableRow = new TableRow(this);
        TextView priority = new TextView(this);
        TextView productId = new TextView(this);
        TextView productName = new TextView(this);
        TextView amount = new TextView(this);

        if (index == 0) {
            setTitleLabel();
        }

        tableRow.setLayoutParams(tableRowParams);
        priority.setLayoutParams(viewParams);
        productId.setLayoutParams(viewParams);
        productName.setLayoutParams(viewParams);
        amount.setLayoutParams(viewParams);

        priority.setGravity(Gravity.CENTER_HORIZONTAL);
        productId.setGravity(Gravity.CENTER_HORIZONTAL);
        productName.setGravity(Gravity.CENTER_HORIZONTAL);
        amount.setGravity(Gravity.CENTER_HORIZONTAL);

        priority.setText(String.valueOf(index + 1));
        productId.setText(productLine.getProductId(index));
        productName.setText(productLine.getProductName(index));
        amount.setText(productLine.getTotal(index));

        priority.setTextSize(Config.TEXT_SIZE);
        productId.setTextSize(Config.TEXT_SIZE);
        productName.setTextSize(Config.TEXT_SIZE);
        amount.setTextSize(Config.TEXT_SIZE);

        tableRow.addView(priority);
        tableRow.addView(productId);
        tableRow.addView(productName);
        tableRow.addView(amount);

        tableLayout.addView(tableRow);
    }

    private void setTitleLabel() {
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1);
        TableRow.LayoutParams viewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow tableRow = new TableRow(this);
        TextView priority = new TextView(this);
        TextView productId = new TextView(this);
        TextView productName = new TextView(this);
        TextView amount = new TextView(this);

        tableRow.setLayoutParams(tableRowParams);
        priority.setLayoutParams(viewParams);
        productId.setLayoutParams(viewParams);
        productName.setLayoutParams(viewParams);
        amount.setLayoutParams(viewParams);

        priority.setText("生產順序");
        productId.setText("產品編號");
        productName.setText("產品名稱");
        amount.setText("所需數量");

        priority.setTextSize(Config.TEXT_TITLE_SIZE);
        productId.setTextSize(Config.TEXT_TITLE_SIZE);
        productName.setTextSize(Config.TEXT_TITLE_SIZE);
        amount.setTextSize(Config.TEXT_TITLE_SIZE);

        priority.setTypeface(null, Typeface.BOLD);
        productId.setTypeface(null, Typeface.BOLD);
        productName.setTypeface(null, Typeface.BOLD);
        amount.setTypeface(null, Typeface.BOLD);

        priority.setTextColor(Color.BLACK);
        productId.setTextColor(Color.BLACK);
        productName.setTextColor(Color.BLACK);
        amount.setTextColor(Color.BLACK);

        tableRow.addView(priority);
        tableRow.addView(productId);
        tableRow.addView(productName);
        tableRow.addView(amount);

        tableLayout.addView(tableRow);
    }
}
