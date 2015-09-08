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

import com.example.medionchou.tobacco.SubFragment.CPFragment;
import com.example.medionchou.tobacco.SubFragment.DeviceFragment;
import com.example.medionchou.tobacco.SubFragment.IngreTitlesFragment;
import com.example.medionchou.tobacco.SubFragment.RecipeTitlesFragment;

/**
 * Created by medionchou on 2015/8/23.
 */
public class LookUpFragment extends Fragment{

    public static final String TAG_TITLE  = "TAG_TITLE";
    public static final String TAG_CONTENT = "TAG_CONTENT";

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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_lookup_layout, container, false);
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

    public void createFragment(Fragment newFrag, int layoutId, String fragTag) {
        FragmentManager fragmentManager= LookUpFragment.this.getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment oldFrag = fragmentManager.findFragmentByTag(fragTag);

        if (oldFrag != null) {
            fragmentTransaction.replace(layoutId, newFrag, fragTag);
        } else {
            fragmentTransaction.add(layoutId, newFrag, fragTag);
        }

        fragmentTransaction.commit();
    }

    public void deleteFragment(String fragTag) {
        FragmentManager fragmentManager= LookUpFragment.this.getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment oldFrag = fragmentManager.findFragmentByTag(fragTag);

        if (oldFrag != null) {
            fragmentTransaction.remove(oldFrag);
            fragmentTransaction.commit();
        }
    }

    private class IngredientBtnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mConnection.isBound()) {
                /*
                    Disconnect with Server;
                 */
            }
            IngreTitlesFragment ingreTitlesFragment = new IngreTitlesFragment();
            ingreTitlesFragment.setParentFrag(LookUpFragment.this);
            createFragment(ingreTitlesFragment, R.id.title_frag_container, TAG_TITLE);
            deleteFragment(TAG_CONTENT);
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
            CPFragment cpFragment = new CPFragment();
            createFragment(cpFragment, R.id.content_frag_container, TAG_CONTENT);
            deleteFragment(TAG_TITLE);
            setTitleFrameLayoutWeight(0f);
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
            RecipeTitlesFragment recipeTitlesFragment = new RecipeTitlesFragment();
            recipeTitlesFragment.setParentFrag(LookUpFragment.this);
            createFragment(recipeTitlesFragment, R.id.title_frag_container, TAG_TITLE);
            deleteFragment(TAG_CONTENT);
            setTitleFrameLayoutWeight(1f);


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

            DeviceFragment deviceFragment = new DeviceFragment();
            createFragment(deviceFragment, R.id.content_frag_container, TAG_CONTENT);
            deleteFragment(TAG_TITLE);
            setTitleFrameLayoutWeight(0f);
        }
    }

}
