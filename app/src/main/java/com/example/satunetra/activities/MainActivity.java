package com.example.satunetra.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.satunetra.R;
import com.example.satunetra.activities.local.RoomHelper;
import com.example.satunetra.activities.praregisted.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isRegister;
    private RoomHelper roomHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomHelper = new RoomHelper(this);

        isRegister = roomHelper.isUserExist();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if(isRegister){

                }else{
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                }
            }},1000);

    }
}