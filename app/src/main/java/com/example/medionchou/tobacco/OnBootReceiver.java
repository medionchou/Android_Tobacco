package com.example.medionchou.tobacco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.medionchou.tobacco.Activity.MainActivity;

/**
 * Created by Medion on 2015/11/17.
 */
public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent myActivity = new Intent(context, MainActivity.class);
            myActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(myActivity);
        }
    }

}
