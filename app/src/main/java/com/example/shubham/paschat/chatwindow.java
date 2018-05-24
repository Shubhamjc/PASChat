package com.example.shubham.paschat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.InputFilter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

//IMPORT JAVA FILES
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class chatwindow extends AppCompatActivity {

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private TextView mChatNameTextView;

    private Button mDisplayMembersButton;

    private String mUsername;
    private String mPhoneNumber;

    private static final int RC_PHOTO_PICKER =  2;

    //FIREBASE INSTANCE VARIABLES
    public FirebaseDatabase mFirebaseDatabase;
    public DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    public FirebaseStorage mFirebaseStorage;
    public StorageReference mChatPhotosStorageReference;
    public FirebaseRemoteConfig mFirebaseRemoteConfig;
    public List<FriendlyMessage> friendlyMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindow);

        //mUsername = ANONYMOUS;

        GlobalClass globalClass = (GlobalClass) getApplicationContext();
        String groupName = globalClass.getActiveGroup();

        //Initialize Firebase Components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        //REFERENCE TO COMPONENTS
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(groupName).child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chatImages");

        //INITIALIZE REFERENCES TO VIEWS
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mDisplayMembersButton = (Button) findViewById(R.id.displayMembersBtn);

        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        mUsername = spref.getString("username",text);
        mPhoneNumber = spref.getString("phoneNumber",text);

        // INITIALIZE MESSAGE LISTVIEW AND ITS ADAPTER
        friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message_left, friendlyMessages,mPhoneNumber);
        mMessageListView.setAdapter(mMessageAdapter);
        mMessageListView.setBackgroundResource(R.color.message_bg);

        mChatNameTextView = (TextView) findViewById(R.id.chatNameTextView);
        mChatNameTextView.setText(groupName);

        // INITIALIZE PROGRESS BAR
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);


        String checkLogin = spref.getString("checkLogin",text);
        if(checkLogin.equals("true"))
            attachDatabaseReadListener();
        else {
            detachDatabaseReadListener();
            mMessageAdapter.clear();
        }
        //TODO: Enable Send Button When There's Text To Send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //TODO: Sends A Message And Clears The Edit Text
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                Date currentLocalTime = cal.getTime();

                // Add "....:SS" For Seconds and "..... EEE" For Day
                DateFormat time = new SimpleDateFormat("kk:mm");
                DateFormat date = new SimpleDateFormat("dd-MM-yyyy");

                // YOU CAN GET SECONDS BY ADDING  "...:SS" TO IT
                time.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));

                String localTime = time.format(currentLocalTime);
                String localDate = date.format(currentLocalTime);

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername,mPhoneNumber, null,localDate,localTime);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // CLEAR INPUT BOX
                mMessageEditText.setText("");
            }
        });

        //TODO: Select And Send Images On Click
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        mDisplayMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMembersList();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            //TODO: Get A Reference To Store File At 'chat_photos/<FILENAME>'
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            //TODO: Upload File On Firebase Storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(
                    this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(),"Photo Uploaded!",Toast.LENGTH_SHORT).show();
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                            Date currentLocalTime = cal.getTime();

                            // Add "....:SS" For Seconds and "..... EEE" For Day
                            DateFormat time = new SimpleDateFormat("kk:mm");
                            DateFormat date = new SimpleDateFormat("dd-MM-yyyy");

                            // YOU CAN GET SECONDS BY ADDING  "...:SS" TO IT
                            time.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));

                            String localTime = time.format(currentLocalTime);
                            String localDate = date.format(currentLocalTime);

                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            FriendlyMessage friendlyMessage = new FriendlyMessage(null,mUsername,mPhoneNumber,downloadUrl.toString(),localDate,localTime);
                            mMessagesDatabaseReference.push().setValue(friendlyMessage);
                        }
                    }
            );
        }
    }

    //TODO: Attach Database Read Listener And Display Data
    private void attachDatabaseReadListener() {
        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        final String userName = spref.getString("username",text);
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.remove(friendlyMessage);
                }
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
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

    private void goToMembersList(){
        startActivity(new Intent(this,GroupMembers.class));
    }

    //TODO: CALL BACKS
    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        attachDatabaseReadListener();
    }

}
