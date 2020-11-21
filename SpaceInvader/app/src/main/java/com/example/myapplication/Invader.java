package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Invader {

    RectF rect;

    Random generator = new Random();

    // The player ship will be represented by a Bitmap
    private Bitmap bitmap;

    // How long and high our invader will be
    private float length;
    private float height;

    // X is the far left of the rectangle which forms our invader
    private float x;

    // Y is the top coordinate
    private float y;

    // This will hold the pixels per second speedthat the invader will move
    private float shipSpeed;
    private int health;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = RIGHT;
    private int type;
    boolean isVisible;

    private float bulletSpeed;
    public Invader(Context context, int row, int column, int screenX, int screenY,int type) {

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX / 20;
        height = screenY / 20;

        isVisible = true;
        this.type=type;
        int padding = screenY / 25;

        x = column * (length + padding);
        y = row * (length + padding/2);

        // Initialize the bitmap

        switch (type){
            case 1:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster1);
                health = 100;
                bulletSpeed=300;
                break;
            case 2:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster2);
                health = 200;
                bulletSpeed=300;
                break;
            case 3:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster3);
                health = 300;
                bulletSpeed=350;
                break;
            case 4:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster4);
                health = 400;
                bulletSpeed=350;
                break;
            case 5:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster5);
                health = 400;
                bulletSpeed=400;
                break;
            case 6:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster6);
                health = 300;
                bulletSpeed=400;
                break;
            case 7:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster7);
                health = 200;
                bulletSpeed=450;
                break;
            case 8:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster8);
                health = 500;
                bulletSpeed=450;
                break;
            case 9:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.monster9);
                health = 400;
                bulletSpeed=500;
                break;

        }
        // stretch the first bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // stretch the first bitmap to a size appropriate for the screen resolution


        // How fast is the invader in pixels per second
        shipSpeed = 40;
    }
    public void setHealth(int health)
    {
        this.health=health;
    }

    public int getHealth()
    {
        return  health;
    }

    public void setType(int type)
    {
        this.type=type;
    }
    public int getType()
    {
        return type;
    }
    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

    public float getBulletSpeed() {
        return bulletSpeed;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }


    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getLength(){
        return length;
    }

    public void update(long fps){
        if(shipMoving == LEFT){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            x = x + shipSpeed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }

    public void dropDownAndReverse(){
        if(shipMoving == LEFT){
            shipMoving = RIGHT;
        }else{
            shipMoving = LEFT;
        }

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength,int type){

        int randomNumber = -1;
        //variable that increase chance of shooting for bigger levels
        int shootMultiplier=600/(type);
        //variable that increase chance of shooting for bigger levels and while player ship is near invaders
        int closeShootMultiplier=600/(type*2);
        // If near the player
        if((playerShipX + playerShipLength > x &&
                playerShipX + playerShipLength < x + length) || (playerShipX > x && playerShipX < x + length)) {
            randomNumber = generator.nextInt(closeShootMultiplier);
            if(randomNumber == 0) {
                return true;
            }

        }

        // If firing randomly (not near the player) a 1 in 300 chance
        randomNumber = generator.nextInt(shootMultiplier);
        return randomNumber == 0;
    }
    //chance to drop an upgrade
    public boolean dropUpgradeChance()
    {
        int randomNumber=-1;
        randomNumber=generator.nextInt(2);
        return randomNumber==0;
    }
}
