package com.bahaa.eventorganizerapp.Fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bahaa.eventorganizerapp.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MoreFragment extends Fragment {

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

        return v;
    }

    @OnClick(R.id.more_event_next)
    void launchMoreInfoFragment(){
        Log.i("statuss", "clicked!");
        LocationFragment locationFragment = new LocationFragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, locationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
