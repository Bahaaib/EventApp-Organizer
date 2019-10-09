package com.bahaa.eventorganizerapp.Fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class MoreFragment extends Fragment {

    private static final int GALLERY_INTENT = 22;

    private ProgressDialog mProgressDialog;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private String hasDateFocus;
    private String hasTimeFocus;

    //Firebase Storage..
    StorageReference storage;

    //Bind Views
    @BindView(R.id.more_event_start_date)
    TextInputEditText startDateField;

    @BindView(R.id.more_event_end_date)
    TextInputEditText endDateField;

    @BindView(R.id.more_event_start_time)
    TextInputEditText startTimeField;

    @BindView(R.id.more_event_end_time)
    TextInputEditText endTimeField;


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
        listenToDatePicker();
        listenToTimePicker();

        return v;
    }

    @OnClick(R.id.more_event_upload_button)
    void getImageFromDisk() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }

    @OnClick(R.id.more_event_start_date)
    void pickStartDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener,
                calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();

        hasDateFocus = "startDatePicker";
    }

    @OnClick(R.id.more_event_end_date)
    void pickEndDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener,
                calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();

        hasDateFocus = "endDatePicker";
    }

    @OnClick(R.id.more_event_start_time)
    void pickStartTime() {
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        getActivity(),
                        onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();

        hasTimeFocus = "startTimePicker";
    }

    @OnClick(R.id.more_event_end_time)
    void pickEndTime() {
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        getActivity(),
                        onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();

        hasTimeFocus = "endTimePicker";
    }

    @OnClick(R.id.more_event_next)
    void launchMoreInfoFragment() {
        LocationFragment locationFragment = new LocationFragment();
        FragmentManager manager = getFragmentManager();
        assert manager != null;
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

    private void listenToDatePicker() {
        calendar = Calendar.getInstance();
        onDateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (hasDateFocus.equals("startDatePicker")) {
                displayDate(startDateField);
            } else {
                displayDate(endDateField);
            }
        };
    }

    private void listenToTimePicker() {
        calendar = Calendar.getInstance();
        onTimeSetListener = (timePicker, hourOfDay, minOfHour) -> {

            if (hasTimeFocus.equals("startTimePicker")) {
                displayTime(startTimeField, hourOfDay, minOfHour);

            } else {
                displayTime(endTimeField, hourOfDay, minOfHour);
            }
        };


    }


    private void displayDate(TextInputEditText field) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.UK);
        field.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private void displayTime(TextInputEditText field, int hour, int min) {
        field.setText(hour + " : " + min);
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
