package com.example.shubham.paschat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class AddMembers extends AppCompatActivity {

    //FIREBASE INSTANCE VARIABLES
    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private String mGroupName;

    private ListView mUserListView;
    private UserListAdapter mUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        mGroupName = globalClass.getActiveGroup();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("Users");

        mUserListView = (ListView) findViewById(R.id.addMemberListView);

        List<LoginData> friendlyMessages = new ArrayList<>();
        mUserListAdapter = new UserListAdapter(this, R.layout.item_user, friendlyMessages);
        mUserListView.setAdapter(mUserListAdapter);

        attachDatabaseReadListener();

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
                    LoginData friendlyMessage = dataSnapshot.getValue(LoginData.class);
                    mUserListAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoginData friendlyMessage = dataSnapshot.getValue(LoginData.class);
                    mUserListAdapter.add(friendlyMessage);
                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    LoginData friendlyMessage = dataSnapshot.getValue(LoginData.class);
                    mUserListAdapter.add(friendlyMessage);
                }
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

    //TODO: Dialog Box To Confirm Whether To Add The Member
    private void displayDialogBox(final View view){
        final SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String phoneNumber = spref.getString("phoneNumber",text);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Add to Group?")
                .setCancelable(true)
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                addMember(view);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //TODO: Add The Selected Member In The Group
    private void addMember(View view){
        TextView userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
        TextView phoneNumberTextView = (TextView) view.findViewById(R.id.phoneNumberTextView);

        final String newUsername = userNameTextView.getText().toString();
        final String newPhoneNumber = phoneNumberTextView.getText().toString();

        final DatabaseReference ref = mFirebaseDatabase.getReference().child(mGroupName).child("Members");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(newPhoneNumber)){
                    Toast.makeText(getApplicationContext(),"Already a Member",Toast.LENGTH_SHORT).show();
                }
                else {
                    LoginData loginData = new LoginData(newUsername,newPhoneNumber,false);
                    ref.child(newPhoneNumber).setValue(loginData);
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
        mUserListAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        attachDatabaseReadListener();
    }

}
