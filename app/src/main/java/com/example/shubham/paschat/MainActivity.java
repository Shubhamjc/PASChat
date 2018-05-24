package com.example.shubham.paschat;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;
    private String mUsername;
    private GroupListAdapter mGroupListAdapter;
    private ListView mGroupListView;
    //FIREBASE INSTANCE VARIABLES
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;

    MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getPermissions();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("Groups");

        mGroupListView = (ListView) findViewById(R.id.groupListView);
        List<GroupData> friendlyMessages = new ArrayList<>();
        mGroupListAdapter = new GroupListAdapter(this, R.layout.item_group, friendlyMessages);
        mGroupListView.setAdapter(mGroupListAdapter);

        //TODO: Check Sign In Status
        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        String checkLogin = spref.getString("checkLogin", text);
        String userName = spref.getString("username",text);

        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        globalClass.setUserName(userName);
        if (checkLogin.equals("true"))
            onSignedInInitialize();
        else {
            startActivity(new Intent(this, signin.class));
        }

        mUsername = spref.getString("phoneNumber", text);

        //TODO: Go To Chat Activity On Selecting A Group
        mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView groupName = (TextView) view.findViewById(R.id.groupTextView);
                String text = groupName.getText().toString();

                GlobalClass globalClass = (GlobalClass) getApplicationContext();
                globalClass.setActiveGroup(text);
                startActivity(new Intent(getApplicationContext(), chatwindow.class));
            }
        });
    }

    // TODO: CALLBACK FUNCTIONS
    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        mGroupListAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onSignedInInitialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.actionSignOut:
                logOut();
                break;

            case R.id.actionUserList:
                startActivity(new Intent(this, userlist.class));
                break;

            case R.id.actionCreateGroup:
                createNewGroupDialogBox();
                break;

            case R.id.action_notify:
                startActivity(new Intent(this,notificationlist.class));
                break;

            case R.id.action_share:
                //TODO: Share Application Using Share Intent
                ApplicationInfo app = getApplicationContext().getApplicationInfo();
                String filePath = app.sourceDir;
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("application/vnd.android.package-archive");

                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                startActivity(Intent.createChooser(intent, "Share app using"));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: Go To Sign In Activity
    private void goLogInScreen() {
        Intent intent = new Intent(this, signin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //TODO: Display Toast
    private void showToast(String errorMessage) {
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    //TODO: Sign Out
    private void logOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = spref.edit();
                            editor.putString("checkLogin", "false");
                            editor.putString("username", "");
                            editor.putString("phoneNumber", "");
                            editor.commit();
                            goLogInScreen();
                        } else {
                            showToast(getResources().getString(R.string.sign_out_failed));
                        }
                    }
                });
    }

    //TODO: Create New Group
    private void createNewGroupDialogBox() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.creategroupdialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText createGroupEditText = (EditText) promptView.findViewById(R.id.createGroupEditText);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Create Group",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String groupName = createGroupEditText.getText().toString();
                                createGroup(groupName);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        //Create Dialog Box
        AlertDialog alertDialog = alertDialogBuilder.create();
        //Show Dialog Box
        alertDialog.show();
    }

    //TODO: If Signed In
    private void onSignedInInitialize() {
        attachDatabaseReadListener();
    }

    //TODO: If Signed Out
    private void onSignedOutCleanup() {
        mGroupListAdapter.clear();
        detachDatabaseReadListener();
    }

    //TODO: Attach Database Read Listener And Display Data
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    GroupData groupData = dataSnapshot.getValue(GroupData.class);
                    displayGroups(groupData);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    GroupData groupData = dataSnapshot.getValue(GroupData.class);
                    displayGroups(groupData);
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    GroupData groupData = dataSnapshot.getValue(GroupData.class);
                    displayGroups(groupData);
                }

                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                }
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

    //TODO: Create New Group
    private void createGroup(final String newGroupName){
        final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber", text);
        final String userName = spref.getString("username",text);

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(newGroupName)){
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                    Date currentLocalTime = cal.getTime();

                    DateFormat date = new SimpleDateFormat("dd-MM-yyyy");

                    String localDate = date.format(currentLocalTime);

                    DatabaseReference mGroupsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
                    GroupData groupList = new GroupData(newGroupName, userName + "(" + phoneNumber + ")",localDate);
                    mGroupsDatabaseReference.child(newGroupName).setValue(groupList);

                    DatabaseReference mMembersDatabaseReference = FirebaseDatabase.getInstance().getReference().child(newGroupName).child("Members");

                    LoginData loginData1 = new LoginData(userName,phoneNumber,true);
                    mMembersDatabaseReference.child(phoneNumber).setValue(loginData1);
                }
                else
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.groupExists),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.errorOccurred),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Add Group To Adapter If User Is A Member
    private void displayGroups(final GroupData groupData){
        final String groupName = groupData.getGroupName();

        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber", text);

        final DatabaseReference mMembersDatabaseReference = FirebaseDatabase.getInstance().getReference().child(groupName).child("Members");
        mMembersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber))
                    mGroupListAdapter.add(groupData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Ask For Permission To Read Contacts
    private void getPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_CONTACTS)) {}
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        1);
            }
        }
    }
}
