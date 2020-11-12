package com.example.myapplication;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Background {

    Bitmap bitmap;
    int width;
    int height;
    float speed;

    int yClip;
    int startX;
    int endX;

    Background(Context context, int screenWidth, int screenHeight, String bitmapName, int sX, int eX, float s) {

        // Make a resource id out of the string of the file name
        int resID = context.getResources().getIdentifier(bitmapName,
                "drawable", context.getPackageName());

        // Load the bitmap using the id
        bitmap = BitmapFactory.decodeResource(context.getResources(), resID);

        //Initialise animation variables.

        // Where to clip the bitmaps
        // Starting at the first pixel
        yClip = 0;

        //Position the background vertically
        startX = sX * (screenWidth / 100);
        endX = eX * (screenWidth / 100);
        speed = s;

        // Create the bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, (endX - startX),
                screenHeight
                , true);

        // Save the width and height for later use
        width = bitmap.getWidth();
        height = bitmap.getHeight();

    }

    public void update(long fps) {


        // Move the clipping position and reverse if necessary
        yClip -= 150 / fps;
        if (yClip >= height) {
            yClip = 0;

        } else if (yClip <= 0) {
            yClip = height;
        }
    }

}
