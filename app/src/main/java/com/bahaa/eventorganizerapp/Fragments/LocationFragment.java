package com.bahaa.eventorganizerapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bahaa.eventorganizerapp.Activities.HomeActivity;
import com.bahaa.eventorganizerapp.Models.EventModel;
import com.bahaa.eventorganizerapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class LocationFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.location_event_address)
    TextInputEditText eventAddressField;

    private MarkerOptions markerOptions;

    //Firebase DB
    private DatabaseReference mRef;

    private EventModel event;
    private Unbinder unbinder;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_location, container, false);

        unbinder = ButterKnife.bind(this, v);

        initFirebaseDB();
        getBundleArguments();
        setupEventLocationMap();

        return v;
    }

    @OnClick(R.id.location_event_save)
    void saveEventToDB(){
        addDataToEventModel();
        String EVENT_DB = "event";
        mRef.child(EVENT_DB).push().setValue(event, (databaseError, databaseReference) -> {
            displayToast("تم أضافة الإيفنت بنجاح");
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initFirebaseDB(){
        FirebaseApp.initializeApp(getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
    }

    private void getBundleArguments(){
        String EVENT_INFO_KEY = "event";
        Bundle bundle = getArguments();

        if (bundle != null){
            event = (EventModel) bundle.getSerializable(EVENT_INFO_KEY);
        }
    }

    private void addDataToEventModel(){
        String address = eventAddressField.getText().toString();
        event.setAddress(address);

    }


    private void setupEventLocationMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.location_event_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(26.563370, 31.695279);
        markerOptions = new MarkerOptions();
        //Add Marker to location
        googleMap.addMarker(
                markerOptions.position(latLng).title("Event Location").draggable(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Zoom in to location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                markerOptions.position(marker.getPosition());
            }
        });

    }

    @OnClick(R.id.location_event_latlng_button)
    void recordLatLng() {
        double lat = markerOptions.getPosition().latitude;
        double lng = markerOptions.getPosition().longitude;
        event.setLatitude(lat);
        event.setLongitude(lng);
        displayToast("تم تسجيل اللوكيشن بنجاح");
    }

    private void displayToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
