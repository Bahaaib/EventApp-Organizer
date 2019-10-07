package com.bahaa.eventorganizerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bahaa.eventorganizerapp.Root.AdapterListener;
import com.bahaa.eventorganizerapp.Root.DialogListener;
import com.bahaa.eventorganizerapp.Root.HeadModel;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeActivity extends AppCompatActivity implements DialogListener, AdapterListener {

    private static final int GALLERY_INTENT = 22;
    ArrayList<HeadModel> headList;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nv)
    NavigationView navigationView;
    //Event image..
    @BindView(R.id.add_img_btn)
    AppCompatButton galleryButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    View header;
    TextView headName;
    //Firebase Storage..
    StorageReference storage;
    @BindView(R.id.events_rv)
    RecyclerView recyclerView;
    private DatabaseReference mRef;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private SharedPreferences preferences;
    private String headPhoneNumber;
    //Navigation Drawer
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ProgressDialog mProgressDialog;
    private boolean isAdmin;
    private Unbinder unbinder;
    private ArrayList<EventModel> eventsList;
    private EventRecyclerAdapter adapter;
    private GridLayoutManager gridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //Firebase DB
        FirebaseApp.initializeApp(this);
        //Firebase DB
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        callEventsDatabase();
        setupEventsRV();

        //Firebase Auth..
        mAuth = FirebaseAuth.getInstance();

        headList = new ArrayList<>();

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        restoreSavedPrefs();

        //Gallery image..
        galleryButton = findViewById(R.id.add_img_btn);
        mProgressDialog = new ProgressDialog(this);

        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_INTENT);
        });

        //Firebase storage
        storage = FirebaseStorage.getInstance().getReference();


        //Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_text);

        navigationView = findViewById(R.id.nv);
        header = navigationView.getHeaderView(0);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.add:
                    if (isAdmin) {
                        moveToPrivilegeActivity();
                    } else {
                        displayToast(getString(R.string.no_admin_text));
                    }
                    return true;

                case R.id.organizers:
                    //moveToNotificationActivity();
                    return true;

                case R.id.logout:
                    displayToast(getString(R.string.logging_out));
                    forceLogout();
                    return true;

                default:
                    return true;
            }
        });

        //Sync with firebase..
        headName = header.findViewById(R.id.user_name);
        callHeadDatabase();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }

            if (uri != null) {
                String path = uri.getLastPathSegment();

                if (!isNetworkConnected()) {
                    displayToast(getString(R.string.check_connection));
                } else {
                    mProgressDialog.setMessage(getString(R.string.upload_progress));
                    mProgressDialog.show();

                    assert path != null;
                    StorageReference offerRef = storage.child("offers").child(path);

                    offerRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {

                        mProgressDialog.dismiss();

                        final Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(uri1 -> {
                            addUriToDatabase(uri1);

                            String uploadMsg = getString(R.string.upload_success);
                            displayToast(uploadMsg);
                        });
                    });
                }
            }
        }

    }

    private void setupEventsRV() {
        eventsList = new ArrayList<>();
        adapter = new EventRecyclerAdapter(this, eventsList);
        recyclerView.setAdapter(adapter);
        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

    }

    private void callEventsDatabase() {
        String EVENTS_DB = "event";
        mRef.child(EVENTS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset List of products
                eventsList.clear();
                fetchEventsDB(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchEventsDB(DataSnapshot dataSnapshot) {
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            EventModel model = db.getValue(EventModel.class);
            eventsList.add(model);
            model.setKey(db.getKey());
            adapter.notifyDataSetChanged();
            Log.i("Statuss", model.getTitle() + " " + model.getTicketsAvailable());
        }
    }

    private void callHeadDatabase() {
        String HEADS_DB = "head";
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
        boolean isMember = false;
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            assert model != null;
            model.setKey(db.getKey());
            headList.add(model);
            if (model.getPhone().equals(headPhoneNumber)) {
                //check admin account in case of De-activation
                isMember = model.getStatus();

                isAdmin = model.getPrivilege().equals("admin");

                headName.setText(model.getName());
            }

            String TAG = "Statuss";
            Log.i(TAG, model.getPhone());
        }


        if (!isMember) {
            displayToast(getString(R.string.priv_changed));
            forceLogout();
        }
    }

    private void restoreSavedPrefs() {
        String PHONE_KEY = "head_phone";
        String EMPTY_KEY = "empty";
        //headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);
        headPhoneNumber = "01009540399";
        Log.i("Statuss", "Num: " + headPhoneNumber);

    }

    private void forceLogout() {
        Log.i("Statuss", "Logging out");
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void addUriToDatabase(Uri uri) {
        // OfferModel offer = new OfferModel();
        //offer.setImgUrl(String.valueOf(uri));
        //mRef.child(OFFER_DB).push().setValue(offer);
    }

    private void moveToPrivilegeActivity() {
        Intent intent = new Intent(HomeActivity.this, AdminActivity.class);
        startActivity(intent);
    }


    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public void onAdminDataChanged(HeadModel head) {

    }

    @Override
    public void onDataRemoved(HeadModel head) {

    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}

