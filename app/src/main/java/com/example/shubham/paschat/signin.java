package com.example.shubham.paschat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class signin extends AppCompatActivity {

    private Button signInPhoneBtn;
    private View mRootView;
    private EditText mUsernameEditText;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    public FirebaseAuth mFirebaseAuth;

    final static int SIGNIN_PHONE_REQEST_CODE=111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        signInPhoneBtn = (Button) findViewById(R.id.signInPhoneBtn);
        mRootView = (View) findViewById(R.id.rootView);
        mUsernameEditText = (EditText) findViewById(R.id.userNameEditText);

        mFirebaseDatabase = FirebaseDatabase.getInstance();                                 //Initialize Database Component
        mFirebaseAuth = FirebaseAuth.getInstance();                                         //Initialize Authentication Component
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");       //Initialize Database Reference Component

        //TODO: Keep Logged In
        SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
        String text = "";
        String checkLogin = spref.getString("checkLogin",text);
        if(checkLogin.equals("true"))
            goMainScreen();
    }

    //TODO: Open Firebase UI For Sign In With Phone Number
    public void signInPhoneAct(View v) {
        String name = mUsernameEditText.getText().toString();
        if (name.equals("") || name.isEmpty())
            Toast.makeText(getApplicationContext(), "Enter Username First", Toast.LENGTH_SHORT).show();
        else {
            SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spref.edit();
            editor.putString("username", name);
            editor.commit();

            GlobalClass globalClass = (GlobalClass) getApplicationContext();
            globalClass.setUserName(name);

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                    Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                            .build(),
                    SIGNIN_PHONE_REQEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //For Sign In With Mobile Phone
        if (requestCode == SIGNIN_PHONE_REQEST_CODE) {
            handleSignInResponse(resultCode, data);
        }
    }

    //TODO: Handle Response Given By Sign In Task
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == ResultCodes.OK) {
            SharedPreferences spref = getSharedPreferences("myfile", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spref.edit();
            editor.putString("checkLogin","true");


            final String phn = mFirebaseAuth.getCurrentUser().getPhoneNumber();
            String text = "";
            final String username = spref.getString("username",text);

            editor.putString("phoneNumber",phn);
            editor.commit();
            
            mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LoginData loginData = new LoginData(username,phn,false);
                    mUsersDatabaseReference.child(phn).setValue(loginData);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
            goMainScreen();
        } else {
            if (response == null) {
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }
            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }
            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }
        showSnackbar(R.string.unknown_sign_in_response);
    }

    //TODO: Go To Main Activity
    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //TODO: Display Snack Bar
    private void showSnackbar(int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finishAffinity();
    }
}
