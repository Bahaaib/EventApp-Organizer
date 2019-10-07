package com.bahaa.eventorganizerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bahaa.eventorganizerapp.Dialogs.AdminDialog;
import com.bahaa.eventorganizerapp.Root.AdapterListener;
import com.bahaa.eventorganizerapp.Root.DialogListener;
import com.bahaa.eventorganizerapp.Root.HeadModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AdminActivity extends AppCompatActivity implements DialogListener, AdapterListener {

    private final String HEADS_DB = "head";
    private final String TAG = "admin_dialg";
    private DatabaseReference mRef;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private boolean isAdmin;
    private SharedPreferences preferences;
    private String headPhoneNumber;
    private ArrayList<HeadModel> headsList;
    private HeadRecyclerAdapter adapter;
    private AdminDialog adminDialog;

    @BindView(R.id.heads_rv)
    RecyclerView recyclerView;
    @BindView(R.id.priv_toolbar)
    Toolbar toolbar;
    @BindView(R.id.admin_add_btn)
    AppCompatButton addButton;

    private Unbinder unbinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        unbinder = ButterKnife.bind(this);

        toolbar = findViewById(R.id.priv_toolbar);
        setSupportActionBar(toolbar);

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        restoreSavedPrefs();


        adminDialog = new AdminDialog();

        addButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG);
            if (prev != null) {
                transaction.remove(prev);
            }

            transaction.add(adminDialog, TAG).commit();
        });

        //Firebase DB
        FirebaseApp.initializeApp(this);
        //Firebase DB
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Firebase Auth..
        mAuth = FirebaseAuth.getInstance();

        headsList = new ArrayList<>();

        callHeadDatabase();

        adapter = new HeadRecyclerAdapter(this, headsList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);


    }

    private void callHeadDatabase() {
        mRef.child(HEADS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset List of products
                headsList.clear();
                fetchData(dataSnapshot);
                fetchPersonalData(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            assert model != null;
            model.setKey(db.getKey());
            headsList.add(model);
            adapter.notifyDataSetChanged();

            Log.i("Statuss", model.getName());
        }


    }

    private void fetchPersonalData(DataSnapshot dataSnapshot) {
        boolean isMember = false;
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            assert model != null;
            model.setKey(db.getKey());
            if (model.getPhone().equals(headPhoneNumber)) {
                //check admin account in case of De-activation
                isMember = model.getStatus();

                //Check if still Admin
                isAdmin = model.getPrivilege().equals("admin");
            }
        }

        if (!isAdmin) {
            displayToast(getString(R.string.priv_changed));
            forceStepBack();
        }

        if (!isMember) {
            displayToast(getString(R.string.priv_changed));
            forceLogout();
        }
    }

    private void forceStepBack() {
        Intent intent = new Intent(AdminActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void forceLogout() {
        Log.i("Statuss", "Logging out");
        mAuth.signOut();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void restoreSavedPrefs() {
        String PHONE_KEY = "head_phone";
        String EMPTY_KEY = "empty";
        //headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);
        headPhoneNumber = "01009540399";

    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onAdminDataChanged(HeadModel head) {
        if (head.getKey() != null) {
            mRef.child(HEADS_DB).child(head.getKey())
                    .child("name")
                    .setValue(head.getName());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("phone")
                    .setValue(head.getPhone());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("privilege")
                    .setValue(head.getPrivilege());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("status")
                    .setValue(head.getStatus());
        } else {
            mRef.child(HEADS_DB).push().setValue(head);
        }
    }

    @Override
    public void onDataRemoved(HeadModel head) {
        //Save head key before destroy the head..
        String headKey = head.getKey();
        head.setKey(null);
        mRef.child(HEADS_DB).child(headKey).setValue(head);
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}