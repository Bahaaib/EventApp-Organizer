package com.bahaa.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_dummy_btn)
    AppCompatButton dummyLoginButton;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        unbinder = ButterKnife.bind(this);
    }

    @OnClick(R.id.login_dummy_btn)
    void performLogin(){
        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
        startActivity(intent);
     }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
