package com.example.medionchou.tobacco;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.awt.font.TextAttribute;

/**
 * Created by medionchou on 2015/8/23.
 */
public class TestFragment extends Fragment{

    private ServiceListener mConnection;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mConnection = (ServiceListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.test_frame_layout, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.testt);

        textView.setText(String.valueOf(mConnection.getLocalServiceConnection().getService().isLoggin()));


        return rootView;
    }
}
