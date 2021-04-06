package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Upgrade {

    float speed=350;
    private float x;
    private float y;
    private final RectF rect;
    private final int width = 30;
    private final int height;
    private boolean isActive;
    private Bitmap bitmap;


    public Upgrade(Context context, int screenY) {

        height = screenY / 20;
        isActive = false;
        rect = new RectF();
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.upgrade);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (75),
                (int) (75),
                false);
    }

    public RectF getRect(){
        return  rect;
    }

    public boolean getStatus(){
        return isActive;
    }
    public Bitmap getBitmap()
    {
        return bitmap;
    }
    public float getX()
    {
        return x;
    }
    public float getY()
    {
        return y;
    }

    public void setInactive(){
        isActive = false;
    }

    public float getImpactPointY(){

            return y + height;
    }

    public void dropUpgrade(float startX, float startY) {
        if (!isActive) {
            x = startX;
            y = startY;
            isActive = true;

        }

    }

    public void update(long fps){
        if(fps!=0) {


            y = y + speed / fps;

            // Update rect
            rect.left = x;
            rect.right = x + width;
            rect.top = y;
            rect.bottom = y + height;
        }
    }
}
