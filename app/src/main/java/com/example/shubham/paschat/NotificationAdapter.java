package com.example.shubham.paschat;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class NotificationAdapter extends ArrayAdapter<NotificationData> {
    public NotificationAdapter(Context context, int resource, List<NotificationData> objects) {
        super(context, resource, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_notification, parent, false);
        }

        TextView dateTextView = (TextView) convertView.findViewById(R.id.notificationDateTextView);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.notificationTimeTextView);
        TextView senderTextView = (TextView) convertView.findViewById(R.id.notificationSenderTextView);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.notificationTitleTextView);
        TextView bodyTextView = (TextView) convertView.findViewById(R.id.notificationBodyTextView);

        TextView titleLabel = (TextView) convertView.findViewById(R.id.notificationTitleLabel);
        TextView dateTimeLabel = (TextView) convertView.findViewById(R.id.notificationDateTimeLabel);
        TextView senderLabel = (TextView) convertView.findViewById(R.id.notificationSenderLabel);
        TextView messageLabel = (TextView) convertView.findViewById(R.id.notificationMessageLabel);

        CardView notificationView = (CardView) convertView.findViewById(R.id.notificationLinearView);

        NotificationData notificationData = getItem(position);

        String notificationType = notificationData.getType();

        int valueColor,labelColor;

        if(notificationType.equals("Meeting")) {
            valueColor = ContextCompat.getColor(getContext(),R.color.meetingValueColor);
            labelColor = ContextCompat.getColor(getContext(),R.color.meetingLabelColor);
            notificationView.setBackgroundResource(R.color.meetingBackground);
        }
        else if(notificationType.equals("Notice")) {
            valueColor = ContextCompat.getColor(getContext(),R.color.noticeValueColor);
            labelColor = ContextCompat.getColor(getContext(),R.color.noticeLabelColor);
            notificationView.setBackgroundResource(R.color.noticeBackground);
        }
        //else if(notificationType.equals("Events"))
        else {
            valueColor = ContextCompat.getColor(getContext(),R.color.eventsValueColor);
            labelColor = ContextCompat.getColor(getContext(),R.color.eventsLabelColor);
            notificationView.setBackgroundResource(R.color.eventsBackground);
        }

        dateTextView.setTextColor(valueColor);
        timeTextView.setTextColor(valueColor);
        senderTextView.setTextColor(valueColor);
        titleTextView.setTextColor(valueColor);
        bodyTextView.setTextColor(valueColor);

        dateTimeLabel.setTextColor(labelColor);
        messageLabel.setTextColor(labelColor);
        senderLabel.setTextColor(labelColor);
        titleLabel.setTextColor(labelColor);

        dateTextView.setText(notificationData.getDate());
        timeTextView.setText(notificationData.getTime());
        senderTextView.setText(notificationData.getSender());
        titleTextView.setText(notificationData.getTitle());
        bodyTextView.setText(notificationData.getMessage());

        return convertView;
    }
}
