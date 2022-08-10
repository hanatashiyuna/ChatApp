package com.example.yunaproject.UI.FlashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yunaproject.R;
import com.example.yunaproject.UI.Home.MainActivity;
import com.example.yunaproject.UI.Sign.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class FlashScreenActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_screen);

        //firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null){
            new CountDownTimer(1500, 1500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    hasUser();
                }
            }.start();
        }else{
            new CountDownTimer(1500, 1500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    login();
                }
            }.start();
        }

    }

    public void login(){
        Intent intent = new Intent(FlashScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void hasUser(){
        Intent intent = new Intent(FlashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}