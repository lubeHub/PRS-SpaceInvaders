package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



public class WelcomeScreen extends AppCompatActivity {
    private Button btnNewGame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcomescreen);
        btnNewGame=findViewById(R.id.btnNewGame);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartGame();
            }
        });
    }
    public void StartGame()
    {
        Intent intent = new Intent(this,GameScreen.class);
        startActivity(intent);
    }


    protected void onDestroy() {
        //stop service and stop music
        stopService(new Intent(WelcomeScreen.this, BackgroundSoundService.class));
        super.onDestroy();
    }
}