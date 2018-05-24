package com.example.shubham.paschat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class GroupListAdapter extends ArrayAdapter<GroupData>{
    public GroupListAdapter(Context context, int resource, List<GroupData> objects) {
        super(context, resource, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_group, parent, false);
        }

        ImageView imageView  = (ImageView) convertView.findViewById(R.id.groupIcon);
        TextView groupName = (TextView) convertView.findViewById(R.id.groupTextView);

        GroupData group = getItem(position);

        groupName.setText(group.getGroupName());

        return convertView;
    }
}