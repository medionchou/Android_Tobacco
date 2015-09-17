package com.example.medionchou.tobacco.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.medionchou.tobacco.Constants.States;
import com.example.medionchou.tobacco.LocalService;
import com.example.medionchou.tobacco.LocalServiceConnection;
import com.example.medionchou.tobacco.MD5;
import com.example.medionchou.tobacco.R;


public class MainActivity extends AppCompatActivity {

    private LocalService mService;
    private EditText accountEditView;
    private EditText pwdEditView;
    private Button loginBtn;
    private LocalServiceConnection mConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initObject();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mConnection.isBound()) {
            unbindService(mConnection);
        }
    }

    private void initObject() {
        accountEditView = (EditText) findViewById(R.id.edit_text_account);
        pwdEditView = (EditText) findViewById(R.id.edit_text_password);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        mConnection = new LocalServiceConnection();
        loginBtn.setOnClickListener(new LogginButtonListener());
    }


    private class LogginButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (mConnection.isBound()) {
                ConnectionAsynTask asynTask = new ConnectionAsynTask();
                asynTask.execute((Void)null);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ConnectionAsynTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        String msg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle(getString(R.string.progress_dialog_waiting));
            progressDialog.setMessage(getString(R.string.logging));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void[] params) {
            mService = mConnection.getService();
            String account = accountEditView.getText().toString();
            String pwd = MD5.getMD5EncryptedString(pwdEditView.getText().toString());
            String cmd = "LOGIN\tMASTER\t" + "test" + "\t" + MD5.getMD5EncryptedString("123") + "<END>"; //Specific Command

            if (mService.getClientState() == States.CONNECT_OK) {
                mService.setCmd(cmd);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e("MyLog", "InterruptedException In ConnectionAsynTask :" + e.toString());
                }

                if (mService.isLoggin()) {
                    Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                } else {
                    msg = getString(R.string.user_info_err);
                }
            } else {
                msg = getString(R.string.no_connection);
            }


            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (msg.length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (msg.contains("未連線到伺服器")) {
                    builder.setTitle("提示");
                    builder.setMessage("暫時無法連線到伺服器\n請稍後重試或確認伺服器狀態後重試。");
                    builder.show();
                } else {
                    builder.setTitle("提示");
                    builder.setMessage("帳號或密碼錯誤。");
                    builder.show();
                }
            }
        }
    }
}
