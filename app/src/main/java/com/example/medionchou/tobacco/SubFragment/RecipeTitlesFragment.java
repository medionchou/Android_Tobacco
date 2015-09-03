package com.example.medionchou.tobacco.SubFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medionchou.tobacco.LookUpFragment;
import com.example.medionchou.tobacco.R;

/**
 * Created by Medion on 2015/9/3.
 */
public class RecipeTitlesFragment extends Fragment {

    private int lastExpandedPos = -1;
    private ExpandableListView expandableListView;
    private LookUpFragment parentFragment;

    public void setParentFrag(LookUpFragment parent) {
        this.parentFragment = parent;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.title_frag_layout, container, false);

        expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandable_list_view);
        ExpandableAdapter expandableAdapter = new ExpandableAdapter();
        expandableListView.setAdapter(expandableAdapter);
        expandableListView.setOnGroupExpandListener(new GroupExpandedListener());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    private class ExpandableAdapter extends BaseExpandableListAdapter {

        private String[] group;
        private String[][] children;
        private final LayoutInflater inflater = getActivity().getLayoutInflater();

        public ExpandableAdapter() {
            group = new String[] {"配料歷史", "加香情況"};
            children = new String [][] {
                    {},
                    {}
            };

        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_group, parent, false);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.list_group);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(getGroup(groupPosition).toString());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment newFrag = null;

                    if (getGroup(groupPosition).equals("配料歷史")) {

                    } else if (getGroup(groupPosition).equals("加香情況")) {
                        newFrag = new ACFragment();


                    }

                    if (newFrag != null)
                        parentFragment.createFragment(newFrag, R.id.content_frag_container, LookUpFragment.TAG_CONTENT);
                }
            });

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public int getGroupCount() {

            return group.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return group[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }


        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private class ViewHolder {
            TextView textView;
        }

    }

    private class GroupExpandedListener implements ExpandableListView.OnGroupExpandListener {
        @Override
        public void onGroupExpand(int groupPosition) {
            if (lastExpandedPos != -1 && groupPosition != lastExpandedPos) {
                expandableListView.collapseGroup(lastExpandedPos);
            }
            lastExpandedPos = groupPosition;
        }
    }
}
