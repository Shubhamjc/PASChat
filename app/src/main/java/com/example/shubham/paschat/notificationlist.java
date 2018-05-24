package com.example.shubham.paschat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class notificationlist extends AppCompatActivity {

    private ListView mNotificationListView;
    private NotificationAdapter mNotificationAdapter;
    Button sendNotificationBtn;

    //FIREBASE INSTANCE VARIABLES
    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mNotificationsDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationlist);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mNotificationsDatabaseReference = mFirebaseDatabase.getReference().child("Notification");

        mNotificationListView = (ListView) findViewById(R.id.notificationListView);
        sendNotificationBtn = (Button) findViewById(R.id.sendNotificationBtn);

        List<NotificationData> friendlyMessages = new ArrayList<>();
        mNotificationAdapter = new NotificationAdapter(this, R.layout.item_notification, friendlyMessages);
        mNotificationListView.setAdapter(mNotificationAdapter);

        attachDatabaseReadListener();
        sendNotificationBtn.setVisibility(View.GONE);

        showNotificationButton();

        sendNotificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSendNotification();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        mNotificationAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        attachDatabaseReadListener();
    }

    //TODO: If Signed In
    private void onSignedInInitialize() {
        attachDatabaseReadListener();
    }

    //TODO: If Signed Out
    private void onSignedOutCleanup() {
        mNotificationAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    NotificationData friendlyMessage = dataSnapshot.getValue(NotificationData.class);
                    mNotificationAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    NotificationData friendlyMessage = dataSnapshot.getValue(NotificationData.class);
                    mNotificationAdapter.add(friendlyMessage);
                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    NotificationData friendlyMessage = dataSnapshot.getValue(NotificationData.class);
                    mNotificationAdapter.add(friendlyMessage);
                }
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                }
            };
            mNotificationsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mNotificationsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void showNotificationButton(){
        DatabaseReference mAdminDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber", text);

        mAdminDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)) {
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    if(loginData.getAdmin())
                        sendNotificationBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToSendNotification(){
        startActivity(new Intent(this,sendNotification.class));
    }

}
