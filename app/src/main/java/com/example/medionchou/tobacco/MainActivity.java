package com.example.medionchou.tobacco;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;


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
                Toast.makeText(MainActivity.this, "未連線到伺服器\n請稍候再試", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ConnectionAsynTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        String msg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("請稍候");
            progressDialog.setMessage("登入中");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void[] params) {
            mService = mConnection.getService();
            String account = accountEditView.getText().toString();
            String pwd = MD5.getMD5EncryptedString(pwdEditView.getText().toString());
            String cmd = "LOGIN TABLET " + account + " " + pwd + "<END>";

            if (mService.getClientState() == States.CONNECT_OK) {
                mService.setCmd(cmd);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Log.e("TobaccoLog", "InterruptedException: sleep interrupted " + e.toString());
                }

                if (mService.isLoggin()) {
                    Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                } else {
                    msg = "帳號或密碼錯誤";
                }
            } else {
                msg = "未連線到伺服器\n請稍候再試";
            }


            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if (msg.length() > 0)
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
