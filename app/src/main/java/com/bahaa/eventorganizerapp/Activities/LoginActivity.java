package com.bahaa.eventorganizerapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.preference.PreferenceManager;

import com.bahaa.eventorganizerapp.Models.HeadModel;
import com.bahaa.eventorganizerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "Statuss";
    private final String HEADS_DB = "head";
    private final String PHONE_KEY = "head_phone";

    //Firebase Auth
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationid;



    //Bind Views
    @BindView(R.id.phone_number)
    TextInputEditText phoneNumberField;

    @BindView(R.id.verification_code)
    TextInputEditText smsCodeVerificationField;

    @BindView(R.id.verification_btn)
    AppCompatButton startVerficationButton;

    @BindView(R.id.login_btn)
    AppCompatButton verifyPhoneButton;

    private ProgressDialog progressDialog;


    //Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ArrayList<HeadModel> headList;

    private SharedPreferences preferences;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        unbinder = ButterKnife.bind(this);

        //Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


        headList = new ArrayList<>();

        callHeadDatabae();

        startVerficationButton.setOnClickListener(v -> {
            final String code = smsCodeVerificationField.getText().toString();
            verifyVerificationCode(code);

        });
        verifyPhoneButton.setOnClickListener(v -> {
            final String phoneNumber = phoneNumberField.getText().toString();
            if (isValidMobileNumber(phoneNumber)) {
                //Check if admin in DB
                if (isHead(phoneNumber)) {
                    //Register to Sharedpreferences first..
                    saveToSharedPreferences(PHONE_KEY, phoneNumber);
                    //Then Go login..
                    progressDialog.setMessage(getString(R.string.login_dialog_text));
                    progressDialog.show();
                    startPhoneNumberVerification(phoneNumber);
                } else {
                    phoneNumberField.setError(getString(R.string.not_auth));
                }

            } else {
                phoneNumberField.setError(getString(R.string.incomplete_number));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                Log.i(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                progressDialog.dismiss();
                displayToast("فشل تسجيل الدخول");
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.i(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

    }

    // validating mobile number format
    private boolean isValidMobileNumber(String number) {
        return Patterns.PHONE.matcher(number).matches() && (number.length() > 10);
    }

    private void callHeadDatabae() {
        mRef.child(HEADS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset List of products
                headList.clear();
                fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            headList.add(model);
            assert model != null;
            Log.i(TAG, model.getName());
        }
    }

    private boolean isHead(String phone) {
        for (HeadModel head : headList) {
            //Check account existence
            if (head.getPhone().equals(phone)) {
                //Check account activity
                if (head.getStatus()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void verifyVerificationCode(String code) {
        Log.i(TAG, "ID: " + verificationid);
        Log.i(TAG, "CODE: " + code);
        try {
            //creating the credential
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            Log.i(TAG, "Credentials: " + credential.toString());
            //signing the user
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            Log.i(TAG, "Couldn't get Credentials!");
        }


    }

    private void saveToSharedPreferences(String key, String value) {
        preferences.edit()
                .putString(key, value)
                .apply();
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+2" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithCredential:success");

                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                        } else {
                            // Sign in failed, display a message and update the UI
                            progressDialog.dismiss();
                            displayToast("Sign in Problem. Try again!");
                            Log.i(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                                smsCodeVerificationField.setError("Invalid code.");

                            }

                        }
                    }
                });
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
