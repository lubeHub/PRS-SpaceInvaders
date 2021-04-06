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
    private Bitmap bitmap2;

    // How long and high our invader will be
    private final float length;
    private final float height;

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
    private  int type;
    boolean isVisible;
    int imageID;
    int imageID2;
    private Bitmap bitmap_explosion;
    public Invader(Context context, int row, int column, int screenX, int screenY,int type) {

        // Initialize a blank RectF
        rect = new RectF();

        if(type==20)
        {

            length = (float)screenX / 3;
            height = (float)screenY / 3;

        }
        else {
            length = (float) screenX / 20;
            height = (float) screenY / 20;
             }

        isVisible = true;
        this.type=type;
        int padding = screenY / 25;

        x = column * (length + padding);
        y = row * (length + (float)padding/2);

        // Initialize the bitmap
        imageID= context.getResources().getIdentifier("monster"+ String.valueOf(type),"drawable",context.getPackageName());
        imageID2= context.getResources().getIdentifier("monster"+ String.valueOf(type)+"m","drawable",context.getPackageName());
        if( type==20) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss);
        }
        else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), imageID2);
        }
        health = type * 100;
        bitmap_explosion = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion);


        // stretch the first bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        bitmap2= Bitmap.createScaledBitmap(bitmap2,
                (int) (length),
                (int) (height),
                false);

        bitmap_explosion = Bitmap.createScaledBitmap(bitmap_explosion,
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

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setExplosion() {
        bitmap=bitmap_explosion;
        bitmap2=bitmap_explosion;
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
    public Bitmap getBitmap2() {return bitmap2;}
    public void update(long fps){
        if(fps!=0) {
            if (shipMoving == LEFT) {
                x = x - shipSpeed / fps;
            }

            if (shipMoving == RIGHT) {
                x = x + shipSpeed / fps;
            }

            // Update rect which is used to detect hits
            rect.top = y;
            rect.bottom = y + height;
            rect.left = x;
            rect.right = x + length;
        }
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

        int randomNumber;
        //variable that increase chance of shooting for bigger levels

        int shootMultiplier=1000/(type);
        //variable that increase chance of shooting for bigger levels and while player ship is near invaders
        int closeShootMultiplier=800/(type*2);
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
        int randomNumber;
        randomNumber=generator.nextInt(20);
        return randomNumber==0;
    }
}
