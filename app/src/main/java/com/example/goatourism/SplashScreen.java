package com.example.goatourism;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
AnimationDrawable animationDrawable;
RelativeLayout layout;
private boolean sessionLoggedIn=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        layout=findViewById(R.id.layout);


        startNextActivity();

    }

    @Override
    protected void onStart() {
        super.onStart();
        layout.setBackgroundResource(R.drawable.gradient_animation);
        animationDrawable= (AnimationDrawable)layout.getBackground();
        animationDrawable.setEnterFadeDuration(100);
        animationDrawable.setExitFadeDuration(1000);
        animationDrawable.start();
        sessionLoggedIn=true;

    }

    private void startNextActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sessionLoggedIn)
                    startActivity(new Intent(SplashScreen.this,SignUp.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                else
                    startActivity(new Intent(SplashScreen.this,Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        },5000);
    }
}
