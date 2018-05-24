package com.example.shubham.paschat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class GroupMemberListAdapter extends ArrayAdapter<LoginData>{
    public GroupMemberListAdapter(Context context, int resource, List<LoginData> objects) {
        super(context, resource, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_groupmembers, parent, false);
        }

        TextView userNameTextView = (TextView) convertView.findViewById(R.id.groupMembersUserNameTextView);
        TextView phoneNumberTextView = (TextView) convertView.findViewById(R.id.groupMembersPhoneNumberTextView);
        TextView adminTextView = (TextView) convertView.findViewById(R.id.groupMembersAdminTextView);

        LoginData member = getItem(position);

        userNameTextView.setText(member.getName());
        phoneNumberTextView.setText(member.getNumber());

        if (member.getAdmin())
            adminTextView.setVisibility(View.VISIBLE);
        else
            adminTextView.setVisibility(View.INVISIBLE);

        return convertView;
    }
}