package com.example.shubham.paschat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.List;

public class userlist extends AppCompatActivity {

    Context context;

    //FIREBASE INSTANCE VARIABLES
    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ListView mUserListView;
    private UserListAdapter mUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);
        context = this;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("Users");

        mUserListView = (ListView) findViewById(R.id.userListView);

        List<LoginData> friendlyMessages = new ArrayList<>();
        mUserListAdapter = new UserListAdapter(this, R.layout.item_user, friendlyMessages);
        mUserListView.setAdapter(mUserListAdapter);

        final SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        String checkLogin = spref.getString("checkLogin",text);
        final String phoneNumber = spref.getString("phoneNumber",text);
        if(checkLogin.equals("true"))
            attachDatabaseReadListener();
        else {
            mUserListAdapter.clear();
            detachDatabaseReadListener();
        }

        mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayDialogBox(view);
            }
        });

    }

    //TODO: Attach Database Read Listener And Display Data
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    LoginData loginData = dataSnapshot.getValue(LoginData.class);
                    if(checkContact(loginData.getNumber()))
                        mUserListAdapter.add(loginData);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoginData loginData = dataSnapshot.getValue(LoginData.class);
                    if(checkContact(loginData.getNumber()))
                        mUserListAdapter.add(loginData);
                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    LoginData loginData = dataSnapshot.getValue(LoginData.class);
                    if(checkContact(loginData.getNumber()))
                        mUserListAdapter.add(loginData);
                }
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    //TODO: Detach Database Read Listener And Clear The Adapter
    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    //TODO: Refresh Activity
    private void refeshActivity(){
        detachDatabaseReadListener();
        mUserListAdapter.clear();
        attachDatabaseReadListener();
    }

    //TODO: Ask For Permission To Call
    private void requestCallPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},1);
    }

    //TODO: Check Whether The User Is An Application Super User
    private void checkAdmin(View view,final String phoneNumber){
        TextView userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
        TextView phoneNumberTextView = (TextView) view.findViewById(R.id.phoneNumberTextView);
        final String newUsername = userNameTextView.getText().toString();
        final String newPhoneNumber = phoneNumberTextView.getText().toString();
        DatabaseReference ref = mFirebaseDatabase.getReference().child("Users");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)){
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    if(loginData.getAdmin())
                        changeAdminStatus(newUsername,newPhoneNumber);
                }
                else
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.notAdmin),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Display Dialog Box To Select The Action To Be Performed
    private void displayDialogBox(final View view){
        final SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber",text);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(getResources().getString(R.string.userlistDailogBoxTitle))
                .setCancelable(true)
                .setNeutralButton("Call",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                callUser(view);
                            }
                        })
                .setPositiveButton("Copy Number",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                copyPhoneNumber(view);
                            }
                        })
                .setNegativeButton("Change Admin Status",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                checkAdmin(view,phoneNumber);
                            }
                        });
        //Create Dialog Box
        AlertDialog alertDialog = alertDialogBuilder.create();
        //Show Dialog Box
        alertDialog.show();
    }

    //TODO: Toggle Admin Status Of The Selected User
    private void changeAdminStatus(final String userName, final String phoneNumber){
         final DatabaseReference databaseReference = mFirebaseDatabase.getReference().child("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)) {
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    LoginData loginData1;
                    if(loginData.getAdmin())
                        loginData1 = new LoginData(userName, phoneNumber, false);
                    else
                        loginData1 = new LoginData(userName, phoneNumber, true);
                    databaseReference.child(phoneNumber).setValue(loginData1);
                    refeshActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Copy The Selected Contact Number
    private void copyPhoneNumber(View view){
        TextView phoneNumber = (TextView) view.findViewById(R.id.phoneNumberTextView);
        String text = phoneNumber.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.copyNUmberToast), Toast.LENGTH_SHORT).show();
    }

    //TODO: Call The Selected User
    private void callUser(View view){
        TextView phoneNumber = (TextView) view.findViewById(R.id.phoneNumberTextView);
        String number = phoneNumber.getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestCallPermission();
        } else
            startActivity(callIntent);
        refeshActivity();
    }

    //TODO: Check If Contact Number Exists In THe Contact List
    private boolean checkContact(String phoneNumber){
        Cursor contactCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
        while((contactCursor.moveToNext())){
            String number = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(phoneNumber.equals(number) || phoneNumber.contains(number))
                return true;
        }
        return false;
    }

    //TODO: Call Backs
    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        mUserListAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    protected void onStop(){
        super.onStop();
        detachDatabaseReadListener();
        mUserListAdapter.clear();
    }
}