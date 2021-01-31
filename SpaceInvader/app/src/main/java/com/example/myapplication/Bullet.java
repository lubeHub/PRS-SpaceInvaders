package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Bullet {

    // Which way is it shooting
    public final int UP = 0;
    public final int DOWN = 1;
    // Going nowhere
    int heading = -1;
    float speed=350;
    private float x;
    private float y;
    private final RectF rect;
    private final int width ;
    private final int height;
    private int bulletDamage=100;
    private boolean isActive;
    private Bitmap bitmap;
    private Context context;
    public Bullet(int screenY, int screenX, Context context) {

        height = screenY / 30;
        isActive = false;
        rect = new RectF();
        width = screenX / 60;
        this.context = context;

        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.bullet), width, height,false);

    }

    public RectF getRect(){
        return  rect;
    }
    public void setBitmap(Bitmap bitmap){ this.bitmap=bitmap;}
    public Bitmap getBitmap(){return  bitmap;}
    public float getX(){return x;}
    public float getY(){return y;}
    public float getWidth(){return width;}
    public float getHeight(){return height;}
    public boolean getStatus(){
        return isActive;
    }
    public void  setBulletDamage(int damage)
    {
        bulletDamage=damage;
    }
    public int getBulletDamage()
    {
        return bulletDamage;
    }
    public void setBulletSpeed(float speed)
    { this.speed=speed;}
    public void setInactive(){
        isActive = false;
    }

    public float getImpactPointY(){
        if (heading == DOWN){
            return y + height;
        }else{
            return  y;
        }

    }

    public boolean shoot(float startX, float startY, int direction) {
        if (!isActive) {
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }

        // Bullet already active
        return false;
    }

    public void update(long fps){
        if(fps!=0) {
            // Just move up or down
            if (heading == UP) {
                y = y - speed / fps;
            } else {
                y = y + speed / fps;
            }

            // Update rect
            rect.left = x;
            rect.right = x + width;
            rect.top = y;
            rect.bottom = y + height;
        }
    }
}