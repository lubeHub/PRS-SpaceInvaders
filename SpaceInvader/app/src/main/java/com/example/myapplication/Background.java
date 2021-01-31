package com.example.myapplication;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;


public class Background {

    Bitmap bitmap;
    int width;
    int height;
    float speed;

    int yClip;
    int startX;
    int endX;


    private int screenHeight;
    private int screenWidth;

    private Bitmap bannerLoading;
    Background(Context context, int screenWidth, int screenHeight, String bitmapName, int sX, int eX, float s) {

        // Make a resource id out of the string of the file name
        int resID = context.getResources().getIdentifier(bitmapName,
                "drawable", context.getPackageName());

        // Load the bitmap using the id
        bitmap = BitmapFactory.decodeResource(context.getResources(), resID);
        this.screenHeight=screenHeight;
        this.screenWidth=screenWidth;
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

        bannerLoading= BitmapFactory.decodeResource(context.getResources(),R.drawable.loading_banner);
        bannerLoading = Bitmap.createScaledBitmap(bannerLoading,
                screenWidth,
                screenHeight/7,
                false);

    }

    public void update(long fps) {

        if(fps!=0){
            // Move the clipping position and reverse if necessary
            yClip -= 150 / fps;
            if (yClip >= height) {
                yClip = 0;

            } else if (yClip <= 0) {
                yClip = height;
            }
        }
    }

}
