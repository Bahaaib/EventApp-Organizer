package com.bahaa.eventorganizerapp.Activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bahaa.eventorganizerapp.Fragments.BasicFragment;
import com.bahaa.eventorganizerapp.R;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        launchBasicInfoFragment();
    }

    private void launchBasicInfoFragment(){
        BasicFragment basicFragment = new BasicFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, basicFragment);
        transaction.commit();
    }

}
