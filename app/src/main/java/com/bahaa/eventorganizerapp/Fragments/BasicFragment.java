package com.bahaa.eventorganizerapp.Fragments;


import android.os.Bundle;
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

public class BasicFragment extends Fragment {

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
        MoreFragment moreFragment = new MoreFragment();
        FragmentManager manager = getFragmentManager();
        assert manager != null;
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, moreFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



}
