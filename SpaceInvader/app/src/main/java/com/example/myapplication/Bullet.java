package com.example.myapplication;

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
    private final int width = 1;
    private final int height;
    private int bulletDamage=100;
    private boolean isActive;

    public Bullet(int screenY) {

        height = screenY / 20;
        isActive = false;
        rect = new RectF();
    }

    public RectF getRect(){
        return  rect;
    }

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

        // Just move up or down
        if(heading == UP){
            y = y - speed / fps;
        }else{
            y = y + speed / fps;
        }

        // Update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
}