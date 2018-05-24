package com.example.shubham.paschat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupMembers extends AppCompatActivity {

    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ListView mMembersListView;
    private GroupMemberListAdapter mMembersAdapter;
    private Button mAddMemberBtn;
    private TextView mGroupName,mGroupCreator,mGroupDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        String groupName = globalClass.getActiveGroup();

        setGroupDetails(groupName);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(groupName).child("Members");

        mMembersListView = (ListView) findViewById(R.id.groupMembersListView);
        mAddMemberBtn = (Button) findViewById(R.id.addMemberBtn);
        mGroupName = (TextView) findViewById(R.id.groupName);
        mGroupCreator = (TextView) findViewById(R.id.groupCreator);
        mGroupDate = (TextView) findViewById(R.id.groupDate);

        mAddMemberBtn.setVisibility(View.GONE);
        showAddMemberButton();

        // INITIALIZE MESSAGE LISTVIEW AND ITS ADAPTER
        List<LoginData> loginData = new ArrayList<>();
        mMembersAdapter = new GroupMemberListAdapter(this, R.layout.item_groupmembers, loginData);
        mMembersListView.setAdapter(mMembersAdapter);

        attachDatabaseReadListener();

        mAddMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AddMembers.class));
            }
        });

        mMembersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    mMembersAdapter.add(loginData);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoginData loginData = dataSnapshot.getValue(LoginData.class);
                    mMembersAdapter.add(loginData);
                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    LoginData loginData = dataSnapshot.getValue(LoginData.class);
                    mMembersAdapter.remove(loginData);
                }
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
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

    //TODO: Check Whether The User Is Group Admin
    private void checkAdmin(final boolean task,View view,final String phoneNumber){
        TextView userNameTextView = (TextView) view.findViewById(R.id.groupMembersUserNameTextView);
        TextView phoneNumberTextView = (TextView) view.findViewById(R.id.groupMembersPhoneNumberTextView);
        final String newUsername = userNameTextView.getText().toString();
        final String newPhoneNumber = phoneNumberTextView.getText().toString();

        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        String groupName = globalClass.getActiveGroup();

        DatabaseReference ref = mFirebaseDatabase.getReference().child(groupName).child("Members");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)) {
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    if(loginData.getAdmin()){
                        if(task)
                            makeAdmin(newUsername, newPhoneNumber);
                        else
                            removeMember(newPhoneNumber);
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.notAdmin), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Display Dialog Box
    private void displayDialogBox(final View view){
        final SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber",text);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(true)
                .setNeutralButton("Remove from Group",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkAdmin(false,view,phoneNumber);
                            }
                        })
                .setPositiveButton("Copy Number",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                copyPhoneNumber(view);
                            }
                        })
                .setNegativeButton("Make/Remove Admin",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                checkAdmin(true,view,phoneNumber);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //TODO: Toggle Admin Status Of The Selected Member
    private void makeAdmin(final String userName, final String phoneNumber){
        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        final String groupName = globalClass.getActiveGroup();

        final DatabaseReference databaseReference = mFirebaseDatabase.getReference().child(groupName).child("Members");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)){
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    if(loginData.getAdmin())
                        databaseReference.child(phoneNumber).setValue(new LoginData(userName,phoneNumber,false));
                    else
                        databaseReference.child(phoneNumber).setValue(new LoginData(userName,phoneNumber,true));
                    refeshActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Remove Selected Member From The Group
    private void removeMember(final String phoneNumber){
        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        final String groupName = globalClass.getActiveGroup();
        final DatabaseReference databaseReference = mFirebaseDatabase.getReference().child(groupName).child("Admins");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber))
                    databaseReference.child(phoneNumber).setValue(null);

                    DatabaseReference tempDatabaseReference = mFirebaseDatabase.getReference().child(groupName).child("Members");
                    tempDatabaseReference.child(phoneNumber).setValue(null);
                    refeshActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Copy Contact Number Of Selected Member
    private void copyPhoneNumber(View view){
        TextView phoneNumber = (TextView) view.findViewById(R.id.groupMembersPhoneNumberTextView);
        String text = phoneNumber.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Phone Number Copied", Toast.LENGTH_SHORT).show();
    }

    //TODO: Refresh Activity
    private void refeshActivity(){
        detachDatabaseReadListener();
        mMembersAdapter.clear();
        attachDatabaseReadListener();
    }

    //TODO: Show Add Member Button If The User Is Group Admin
    private void showAddMemberButton(){
        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        final String groupName = globalClass.getActiveGroup();

        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber", text);

        final DatabaseReference databaseReference = mFirebaseDatabase.getReference().child(groupName).child("Members");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(phoneNumber)) {
                    LoginData loginData = dataSnapshot.child(phoneNumber).getValue(LoginData.class);
                    if(loginData.getAdmin())
                        mAddMemberBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Show Group Details
    private void setGroupDetails(final String groupName){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(groupName)){
                    GroupData groupData = dataSnapshot.child(groupName).getValue(GroupData.class);
                    mGroupName.setText(groupData.getGroupName());
                    mGroupCreator.setText(groupData.getGroupCreatedBy());
                    mGroupDate.setText(groupData.getDateCreated());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: Call Backs
    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        mMembersAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        attachDatabaseReadListener();
    }
}
