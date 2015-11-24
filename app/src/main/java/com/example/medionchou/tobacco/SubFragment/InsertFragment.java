package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.medionchou.tobacco.Constants.Command;
import com.example.medionchou.tobacco.Constants.Config;
import com.example.medionchou.tobacco.DataContainer.LineState;
import com.example.medionchou.tobacco.DataContainer.ProductLine;
import com.example.medionchou.tobacco.DataContainer.RecipeList;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.R;
import com.example.medionchou.tobacco.ServiceListener;

import java.util.ArrayList;
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
        private List<RecipeList> recipeLists = new ArrayList<>();
        private List<LineState> lineStateList = new ArrayList<>();


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
                sendCommand(Command.SWAP);
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
                            } else if (tmp.contains("UPDATE_SWAP")) {
                                parseLineState(tmp, true);
                            }
                        }

                        publishProgress("", "");
                    }

                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Log.e("MyLog", "InsertFragment " + e.toString());
            }
            return (Void) null;
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

            TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.production_row_item, null);
            TextView lineNum = (TextView) tableRow.findViewById(R.id.line_num);
            TextView cur_production = (TextView) tableRow.findViewById(R.id.current_production);
            TextView next_production = (TextView) tableRow.findViewById(R.id.next_production);
            Button insert = (Button) tableRow.findViewById(R.id.insert);

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

            if (!isSwapping(i)) {
                insert.setEnabled(false);
                insert.setText("正在換牌中");
            }


            tableLayout.addView(tableRow);
        }

        private boolean isSwapping(int index) {
            LineState cm = lineStateList.get(index * 2);
            LineState pm = lineStateList.get(index * 2 + 1);


            if (cm.getStatus() == 0 && pm.getStatus() == 0)
                return true;



            return false;
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
            AlertDialog.Builder builder;
            AlertDialog alert;
            List<String> tmp = new ArrayList<>();

            public ModificationListener(ProductLine productLine) {
                this.productLine = productLine;
                builder = new AlertDialog.Builder(getActivity());
                alert = builder.create();

                for (int i = 0;i < recipeLists.size(); i++) {
                    tmp.add(recipeLists.get(i).getProductName());
                }
            }

            @Override
            public void onClick(View v) {
                alert.setView(getCustomLayout());
                alert.setTitle("插牌及刪除功能");
                alert.show();
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
                TextView identity = (TextView) tableRow.findViewById(R.id.identifier);
                TextView productName = (TextView) tableRow.findViewById(R.id.product_name);
                TextView quantity = (TextView) tableRow.findViewById(R.id.quantity);
                TextView delete = (TextView) tableRow.findViewById(R.id.delete);
                TextView modify = (TextView) tableRow.findViewById(R.id.modify);

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

                delete.setOnClickListener(new DeletionListener(productLine.getCategory(), productLine.getLineNum(), Integer.toString(i)));
                modify.setOnClickListener(new InsertionListener(productLine.getCategory(), productLine.getLineNum(), Integer.toString(i)));

                return tableRow;
            }

            private class InsertionListener implements View.OnClickListener {

                final int SIZE = 9;

                String category;
                String lineNum;
                String indexOfInsertion;
                AlertDialog.Builder insertionAlertbuilder;
                AlertDialog insertionAlert;
                char[] checkBit;
                char filterType;
                int indexOfSelectedProduct;

                public InsertionListener(String category, String lineNum, String index) {
                    this.category = category;
                    this.lineNum = lineNum;
                    this.indexOfInsertion = index;
                    insertionAlertbuilder = new AlertDialog.Builder(getActivity());


                    checkBit = new char[SIZE];

                    for (int i = 0; i < SIZE; i++) {
                        checkBit[i] = '0';
                    }
                }

                @Override
                public void onClick(View v) {
                    final RelativeLayout relativeLayout = createCustomView();
                    insertionAlert = insertionAlertbuilder.create();
                    insertionAlert.setView(relativeLayout);
                    insertionAlert.setTitle("發佈產品");
                    insertionAlert.show();

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;

                    insertionAlert.getWindow().setAttributes(lp);
                }

                private RelativeLayout createCustomView() {

                    RelativeLayout relativeLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.insertion_layout, null);
                    Spinner spinner = (Spinner) relativeLayout.findViewById(R.id.spinner);
                    RadioGroup radioGroup = (RadioGroup)relativeLayout.findViewById(R.id.radiogroup);
                    EditText editText = (EditText)relativeLayout.findViewById(R.id.numberOf);
                    TextView numberOfBoxes = (TextView)relativeLayout.findViewById(R.id.numberOfBoxes);
                    TextView totalTextView = (TextView)relativeLayout.findViewById(R.id.total);
                    Button confirm = (Button)relativeLayout.findViewById(R.id.send);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, tmp);
                    ItemSelectedListener listener = new ItemSelectedListener();
                    CheckBox[] checkBoxes = new CheckBox[SIZE];
                    int[] ids = {R.id.line1, R.id.line2, R.id.line3, R.id.line4, R.id.line5, R.id.line6, R.id.line7, R.id.line8, R.id.line9};

                    radioGroup.setOnCheckedChangeListener(new RadioButtonListener());

                    for (int i = 0; i < SIZE; i++) {
                        checkBoxes[i] = (CheckBox) relativeLayout.findViewById(ids[i]);
                        checkBoxes[i].setOnCheckedChangeListener(new CheckBoxListener(i, editText, totalTextView));
                    }


                    adapter.setDropDownViewResource(R.layout.spinner_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(listener);

                    editText.addTextChangedListener(new EditTextWatcher(numberOfBoxes, totalTextView));
                    confirm.setOnClickListener(new SendListener(totalTextView));

                    return relativeLayout;
                }

                private class ItemSelectedListener implements Spinner.OnItemSelectedListener {

                    public ItemSelectedListener() {

                    }

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        indexOfSelectedProduct = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }

                private class RadioButtonListener implements RadioGroup.OnCheckedChangeListener {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId){
                            case R.id.f1:
                                filterType = '1';
                                break;
                            case R.id.manual:
                                filterType = 'H';
                                break;
                        }
                    }
                }

                private class CheckBoxListener implements  CheckBox.OnCheckedChangeListener {

                    int index;
                    EditText editText;
                    TextView totalTextView;
                    public CheckBoxListener(int index, EditText editText, TextView total) {
                        this.index = index;
                        this.editText = editText;
                        this.totalTextView = total;
                    }

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int length = totalTextView.getText().toString().length();
                        int totalNumber = Integer.valueOf(totalTextView.getText().toString().substring(2, length - 2));
                        int boxes = 0;

                        if (!editText.getText().toString().equals(""))
                            boxes = Integer.valueOf(editText.getText().toString()) * 200;

                        if (isChecked) {
                            checkBit[index] = '1';
                            totalNumber +=  boxes;
                        } else {
                            checkBit[index] = '0';
                            totalNumber -=  boxes;
                        }

                        totalTextView.setText("共 " + String.valueOf(totalNumber) + " 箱");
                    }
                }

                private class EditTextWatcher implements TextWatcher {
                    TextView numberOfBoxes;
                    TextView totalTextView;

                    public EditTextWatcher(TextView numberOfBoxes, TextView total) {
                        this.numberOfBoxes = numberOfBoxes;
                        this.totalTextView = total;
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int totalLine = 0;
                        int boxes = 0;

                        if (!s.toString().equals(""))
                            boxes = 200 * Integer.valueOf(s.toString());

                        for (int i = 0; i < SIZE; i++) {
                            totalLine += Character.getNumericValue(checkBit[i]);
                        }

                        numberOfBoxes.setText("每條線生產 " + String.valueOf(boxes) + " 箱");
                        totalTextView.setText("共 " + String.valueOf(boxes * totalLine) + " 箱");
                    }
                }

                private class SendListener implements View.OnClickListener {

                    TextView totalTextView;
                    int count;

                    public SendListener(TextView totalTextView) {
                        this.totalTextView = totalTextView;
                        count = 0;
                    }

                    @Override
                    public void onClick(View v) {
                        String command = "";
                        AlertDialog.Builder warning = new AlertDialog.Builder(getActivity());

                        warning.setTitle("警告");
                        if (isLineChecked()) {
                            if (isFilterChoosed()) {
                                if (isBoxEntered()) {
                                    command += "EXE\tRECIPE_INSERT\t" + productLine.getCategory() + "\t" + productLine.getLineNum() + "\t"+ String.valueOf(indexOfInsertion) + "\t";
                                    command += getInsertLineNum() + "\t" + filterType + "\t";
                                    command += recipeLists.get(indexOfSelectedProduct).getSerialNum() + "\t" + getTotalOfBoxes() + "<END>";
                                    insertionAlert.dismiss();
                                    alert.dismiss();
                                    mService.setCmd(command);
                                    warning.setMessage("插牌成功");
                                } else {
                                    warning.setMessage("請輸入班數");
                                }
                            } else {
                                warning.setMessage("請選擇濾嘴");
                            }
                        } else {
                            warning.setMessage("請選擇生產線");
                        }
                        warning.show();
                    }

                    private boolean isLineChecked() {
                        boolean res = false;

                        for (int i = 0; i < SIZE; i++) {
                            if (checkBit[i] == '1') {
                                count += 1;
                                res = true;
                            }
                        }

                        return res;
                    }

                    private boolean isFilterChoosed() {
                        if (filterType == 'H' || filterType == '1')
                            return true;

                        return false;
                    }

                    private boolean isBoxEntered() {
                        int length = totalTextView.getText().toString().length();
                        int number = Integer.valueOf(totalTextView.getText().toString().substring(2, length - 2));

                        if (number == 0)
                            return false;

                        return true;
                    }

                    private String getInsertLineNum() {
                        String lineNum = "";
                        int cnt = 0;

                        for (int i = 0; i < SIZE; i++) {
                            if (checkBit[i] == '1') {
                                lineNum += String.valueOf(i + 1);
                                if (cnt < count-1)
                                    lineNum += ",";
                                cnt++;
                            }
                        }
                        return lineNum;
                    }

                    private String getTotalOfBoxes() {
                        int length = totalTextView.getText().toString().length();
                        String number = totalTextView.getText().toString().substring(2, length - 2);

                        return number;
                    }
                }
            }

            private class DeletionListener implements View.OnClickListener {

                String category;
                String lineNum;
                String indexOfDeletion;

                public DeletionListener(String category, String lineNum, String index) {
                    this.category = category;
                    this.lineNum = lineNum;
                    this.indexOfDeletion = index;
                }

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("警告");
                    builder.setMessage("是否確認刪除？");
                    builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mService.setCmd("EXE\tRECIPE_DELETE\t" + category + "\t" + lineNum + "\t" + indexOfDeletion + "<END>");
                            alert.cancel();
                        }
                    });

                    builder.show();
                }
            }
        }
    }
}

