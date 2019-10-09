package com.bahaa.eventorganizerapp.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bahaa.eventorganizerapp.Models.EventModel;
import com.bahaa.eventorganizerapp.R;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BasicFragment extends Fragment {

    @BindView(R.id.basic_event_title)
    TextInputEditText eventTitleField;

    @BindView(R.id.basic_event_desc)
    TextInputEditText eventDescField;

    @BindView(R.id.basic_event_organizer)
    TextInputEditText eventOrganizerField;

    private EventModel event;
    private Unbinder unbinder;

    public BasicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_basic, container, false);

        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @OnClick(R.id.basic_event_next)
    void launchMoreInfoFragment(){
        addDataToEventModel();

        Bundle bundle = new Bundle();
        String EVENT_INFO_KEY = "event";
        bundle.putSerializable(EVENT_INFO_KEY, event);

        MoreFragment moreFragment = new MoreFragment();
        moreFragment.setArguments(bundle);
        FragmentManager manager = getFragmentManager();

        assert manager != null;
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, moreFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void addDataToEventModel(){
        String title = eventTitleField.getText().toString();
        String desc = eventDescField.getText().toString();
        String organizer = eventOrganizerField.getText().toString();

        event = new EventModel();
        event.setTitle(title);
        event.setDescription(desc);
        event.setOrganizer(organizer);
    }

}
