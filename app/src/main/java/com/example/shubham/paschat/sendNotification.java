package com.example.shubham.paschat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class sendNotification extends AppCompatActivity {

    private EditText mMessageEditText;
    private EditText mTitleEditText;
    private Button mSendButton;
    private Spinner mSpinner;

    //FIREBASE INSTANCE VARIABLES
    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("Notification");

        mMessageEditText = (EditText) findViewById(R.id.notificationBodyEditText);
        mTitleEditText = (EditText) findViewById(R.id.notificationTitleEditText);
        mSendButton = (Button) findViewById(R.id.uploadNotificationBtn);
        mSpinner = (Spinner) findViewById(R.id.notificationType);

        String[] type = {"Meeting","Notice","Events"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,type);
        mSpinner.setAdapter(arrayAdapter);

        // SEND BUTTON SENDS A MESSAGE AND CLEARS THE EDITTEXT
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                Date currentLocalTime = cal.getTime();

                // Add "....:SS" For Seconds and "..... EEE" For Day
                DateFormat time = new SimpleDateFormat("kk:mm");
                DateFormat date = new SimpleDateFormat("dd-MM-yyyy");

                // YOU CAN GET SECONDS BY ADDING  "...:SS" TO IT
                time.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));

                String localTime = time.format(currentLocalTime);
                String localDate = date.format(currentLocalTime);

                String title = mTitleEditText.getText().toString();
                String message = mMessageEditText.getText().toString();

                SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
                String text = "";
                String phoneNumber = spref.getString("phoneNumber",text);
                String username = spref.getString("username",text);

                String sender = username + "(" + phoneNumber + ")";

                String notificationType = mSpinner.getSelectedItem().toString();

                if(title.equals("") || message.equals(""))
                    Toast.makeText(getApplicationContext(),"Field cannot be Empty",Toast.LENGTH_SHORT).show();
                else {
                    NotificationData notificationData = new NotificationData(localDate, localTime, sender, title, message, notificationType);
                    mMessagesDatabaseReference.push().setValue(notificationData);
                    // CLEAR INPUT BOX
                    mMessageEditText.setText("");
                    mTitleEditText.setText("");
                    Toast.makeText(getApplicationContext(),"Notification Sent",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
