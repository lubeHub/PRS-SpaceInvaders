package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

/*
@param length Length of the ship
 */
public class Player {
    //context
    Context context;
    private final RectF rect;
    /*
        @speed - speed of ship
        @bitmap - image of ship
     */

    private Bitmap bitmap;
    /*
      @length - ship length
      @height - ship height
     */
    private final float length;
    private final float height;
    /* @x - far left of the rectangle which forms ship
       @y - top coordinate
    * */
    private float x;
    private float y;
    // Is the ship moving and in which direction
    private boolean shipMoving = false;
    //Boundaries of screen

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public Player(Context context, int screenX, int screenY) {

        // Initialize a blank RectF
        rect = new RectF();


        length = (float)screenX / 10;
        height = (float)screenY / 10;

        this.context = context;

        // Start ship in roughly the screen centre
        x = (float)screenX / 2;
        y = screenY - height;

        // Initialize the bitmap
        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.player),(int)(length),(int)(height),false);



    }

    public RectF getRect() {
        return rect;
    }

    // This is a getter method to make the rectangle that
    // defines our ship available in SpaceInvadersView class
    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getHeight() {
        return height;
    }


    public float getLength() {
        return length;
    }

    // This method will be used to change/set if the ship is going left, right or nowhere
    public void setMovementState(boolean state) {
        shipMoving = state;
    }


    // This update method will be called from update in SpaceInvadersView
    // It determines if the player ship needs to move and changes the coordinates
    public void update(long fps) {

        if (shipMoving) {
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.playermove),(int)(length),(int)(height),false);

        } else {
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.player),(int)(length),(int)(height),false);
        }

        // Update rect which is used to detect hits
        rect.left = x;
        rect.top = y;
        rect.bottom = y + height;
        rect.right = x + length;

    }

}
