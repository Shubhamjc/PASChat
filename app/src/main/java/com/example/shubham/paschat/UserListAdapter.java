package com.example.shubham.paschat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<LoginData> {

    public UserListAdapter(Context context, int resource, List<LoginData> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_user, parent, false);
        }

        TextView mNameTextView = (TextView) convertView.findViewById(R.id.userNameTextView);
        TextView mPhoneTextView = (TextView) convertView.findViewById(R.id.phoneNumberTextView);
        TextView adminTextView = (TextView) convertView.findViewById(R.id.groupMembersAdminTextView);

        LoginData user = getItem(position);

        mNameTextView.setText(user.getName());
        mPhoneTextView.setText(user.getNumber());

        if (user.getAdmin())
            adminTextView.setVisibility(View.VISIBLE);
        else
            adminTextView.setVisibility(View.INVISIBLE);

        return convertView;
    }
}
