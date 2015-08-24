package com.example.medionchou.tobacco;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Created by medionchou on 2015/8/23.
 */
public class LookUpFragment extends Fragment{


    int mNum;
    private ServiceListener mConnection;
    private Button ingredientBtn;
    private Button productionBtn;
    private Button deviceBtn;
    private FrameLayout titleFrameLayout;
    private FrameLayout contentFrameLayout;


    public static LookUpFragment newInstance(int num) {
        LookUpFragment f = new LookUpFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mConnection = (ServiceListener)mConnection;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.look_up_fragment, container, false);

        ingredientBtn = (Button) rootView.findViewById(R.id.ingredient_btn);
        productionBtn = (Button) rootView.findViewById(R.id.production_btn);
        deviceBtn = (Button) rootView.findViewById(R.id.device_status_btn);
        ingredientBtn.setOnClickListener(new IngredientBtnListener());
        productionBtn.setOnClickListener(new ProductionBtnListener());
        deviceBtn.setOnClickListener(new DeviceBtnListener());
        return rootView;
    }


    private class IngredientBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class ProductionBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class DeviceBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager= LookUpFragment.this.getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.title_frag_container,  new TestFragment());
            fragmentTransaction.add(R.id.content_frag_container,  new TestFragment());

            fragmentTransaction.commit();

        }
    }

}
