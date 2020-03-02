package com.kassette;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Window;


public class SplashScreenActivity extends ActionBarActivity {

    //Splash screen timer
    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    public void onStart(){
        super.onStart();

        //Hide the actionbar before displaying the splash screen.
        getSupportActionBar().hide();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Setting the window format allows the splash screen radial background
        // to look good on different screens.
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);

        // This is where we enable the timer for the Splash screen.
        // using a post delayed handler.

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Once the the timer runs out we want to go from SplashScreen activity
                // to the MainActivity.
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);

                //End this current activity.
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
