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
import android.widget.TableRow;

import com.example.medionchou.tobacco.SubFragment.DeviceFragment;
import com.example.medionchou.tobacco.SubFragment.TitlesFragment;

/**
 * Created by medionchou on 2015/8/23.
 */
public class LookUpFragment extends Fragment{
    private final String TAG_TITLE  = "TAG_TITLE";
    private final String TAG_CONTENT = "TAG_CONTENT";



    int mNum;
    private LocalServiceConnection mConnection;
    private Button ingredientBtn;
    private Button productionBtn;
    private Button recipeBtn;
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
        ServiceListener mCallBack;
        mCallBack = (ServiceListener) activity;

        mConnection = mCallBack.getLocalServiceConnection();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lookup_frag_layout, container, false);
        createView(rootView);

        return rootView;
    }

    private void createView(View rootView) {
        ingredientBtn = (Button) rootView.findViewById(R.id.ingredient_btn);
        productionBtn = (Button) rootView.findViewById(R.id.production_btn);
        recipeBtn = (Button) rootView.findViewById(R.id.recipe_status_btn);
        deviceBtn = (Button) rootView.findViewById(R.id.device_status_btn);
        ingredientBtn.setOnClickListener(new IngredientBtnListener());
        productionBtn.setOnClickListener(new ProductionBtnListener());
        recipeBtn.setOnClickListener(new RecipeBtnListener());
        deviceBtn.setOnClickListener(new DeviceBtnListener());

        titleFrameLayout = (FrameLayout) rootView.findViewById(R.id.title_frag_container);
        contentFrameLayout = (FrameLayout) rootView.findViewById(R.id.content_frag_container);
    }

    private void setTitleFrameLayoutWeight(float weight) {

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
        titleFrameLayout.setLayoutParams(params);
    }

    private class IngredientBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mConnection.isBound()) {
                /*
                    Disconnect with Server;
                 */
            }
            FragmentManager fragmentManager= LookUpFragment.this.getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (fragmentManager.findFragmentByTag(TAG_TITLE) != null) {
                fragmentTransaction.replace(R.id.title_frag_container, new TitlesFragment(), TAG_TITLE);
            } else {
                fragmentTransaction.add(R.id.title_frag_container, new TitlesFragment(), TAG_TITLE);
            }

            fragmentTransaction.commit();
            setTitleFrameLayoutWeight(1f);
        }
    }

    private class ProductionBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mConnection.isBound()) {
                /*
                    Disconnect with Server;
                 */
            }
        }
    }

    private class RecipeBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mConnection.isBound()) {
                /*
                    Disconnect with Server;
                 */
            }


        }
    }

    private class DeviceBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (!mConnection.isBound()) {
                /*
                    Disconnect with Server;
                 */
            }

            FragmentManager fragmentManager= LookUpFragment.this.getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment;
            if ((fragment = fragmentManager.findFragmentByTag(TAG_TITLE)) != null) {
                fragmentTransaction.remove(fragment);
            }

            if (fragmentManager.findFragmentByTag(TAG_CONTENT) != null) {
                fragmentTransaction.replace(R.id.content_frag_container, new DeviceFragment(), TAG_CONTENT);
            } else {
                fragmentTransaction.add(R.id.content_frag_container, new DeviceFragment(), TAG_CONTENT);
            }

            fragmentTransaction.commit();

            setTitleFrameLayoutWeight(0f);
        }
    }



}
