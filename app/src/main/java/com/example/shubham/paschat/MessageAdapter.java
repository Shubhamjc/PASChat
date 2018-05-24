package com.example.shubham.paschat;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage>{
    private String mPhoneNumber;
    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects,String phoneNumber) {
        super(context, resource, objects);
        mPhoneNumber = phoneNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendlyMessage message = getItem(position);

        if (mPhoneNumber.equals(message.getNumber())) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_right, parent, false);
        }
        else
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_left, parent, false);

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
        View messageView = (View) convertView.findViewById(R.id.messageView);

        int authorColor;
        String number = message.getNumber().substring(6,7);

        if(mPhoneNumber.equals(message.getNumber()))
            authorColor = ContextCompat.getColor(getContext(),R.color.selfColor);
        else if(Integer.parseInt(number)%4==0)
            authorColor = ContextCompat.getColor(getContext(),R.color.color1);
        else if(Integer.parseInt(number)%4==1)
            authorColor = ContextCompat.getColor(getContext(),R.color.color2);
        else if(Integer.parseInt(number)%4==2)
            authorColor = ContextCompat.getColor(getContext(),R.color.color3);
        else
            authorColor = ContextCompat.getColor(getContext(),R.color.color4);


        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(getContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100*1024*1024)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(imageLoaderConfiguration);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            imageLoader.displayImage(message.getPhotoUrl(), photoImageView, defaultOptions);
           /* Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);*/
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
        authorTextView.setTextColor(authorColor);

        timeTextView.setText(message.getTime());

        dateTextView.setText(message.getDate());

        return convertView;
    }
}