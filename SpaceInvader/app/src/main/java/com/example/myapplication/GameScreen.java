package com.example.myapplication;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;

import java.util.concurrent.CompletableFuture;

public class GameScreen extends Activity{
    SpaceInvadersEngine spaceInvadersEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.game_screen);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);
        spaceInvadersEngine = new SpaceInvadersEngine(this, size.x, size.y);
        setContentView(spaceInvadersEngine);

    }

        // This method executes when the player starts the game
        @Override
        protected void onResume() {
            super.onResume();

            // Tell the gameView resume method to execute
            spaceInvadersEngine.resume();
        }

        // This method executes when the player quits the game
        @Override
        protected void onPause() {
            super.onPause();

            // Tell the gameView pause method to execute
            spaceInvadersEngine.pause();
        }



}

