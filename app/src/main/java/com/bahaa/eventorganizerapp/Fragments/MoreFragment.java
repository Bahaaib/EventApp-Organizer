package com.bahaa.eventorganizerapp.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bahaa.eventorganizerapp.Models.EventModel;
import com.bahaa.eventorganizerapp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class MoreFragment extends Fragment {

    private static final int GALLERY_INTENT = 22;

    private ProgressDialog mProgressDialog;

    //Firebase Storage..
    StorageReference storage;


    Unbinder unbinder;

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_more, container, false);

        unbinder = ButterKnife.bind(this, v);

        initFirebaseStorage();
        setupProgressBar();

        return v;
    }

    @OnClick(R.id.more_event_upload_button)
    void getImageFromDisk() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }

    @OnClick(R.id.more_event_next)
    void launchMoreInfoFragment() {
        Log.i("statuss", "clicked!");
        LocationFragment locationFragment = new LocationFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, locationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupProgressBar() {
        mProgressDialog = new ProgressDialog(getActivity());

    }

    private void addUriToDatabase(Uri uri) {
        EventModel event = new EventModel();
        event.setImage(String.valueOf(uri));
        Log.i("statuss", "ImageURI " + uri);
    }

    private void initFirebaseStorage() {
        storage = FirebaseStorage.getInstance().getReference();
    }

    private void displayToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
                .show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                    StorageReference offerRef = storage.child("events").child(path);

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
}
