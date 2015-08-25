package com.example.medionchou.tobacco;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Medion on 2015/8/25.
 */
public class TitlesFragment extends Fragment {

    private int lastExpandedPos = -1;
    private ExpandableListView expandableListView;

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
            group = new String[] {"本日進出貨情況", "庫存情形", "查詢歷史紀錄"};
            children = new String [][] {
                    {"3號倉庫", "5號倉庫", "6號倉庫", "線邊倉"},
                    {"3號倉庫", "5號倉庫", "6號倉庫", "線邊倉"},
                    {"3號倉庫", "5號倉庫", "6號倉庫", "線邊倉"}
            };

        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
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



            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.list_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(getChild(groupPosition, childPosition).toString());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getGroup(groupPosition).toString().equals("本日進出貨情況")) {
                        Toast.makeText(getActivity(), viewHolder.textView.getText().toString(), Toast.LENGTH_SHORT).show();
                    } else if (getGroup(groupPosition).toString().equals("庫存情形")) {
                        Toast.makeText(getActivity(), viewHolder.textView.getText().toString(), Toast.LENGTH_SHORT).show();
                    } else if (getGroup(groupPosition).toString().equals("查尋歷史紀錄")) {
                        Toast.makeText(getActivity(), viewHolder.textView.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return convertView;
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
            return children[groupPosition][childPosition];
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
            return true;
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
