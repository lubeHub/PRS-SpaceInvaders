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
    private RectF rect;
    /*
        @speed - speed of ship
        @bitmap - image of ship
     */
    private float speed;
    private Bitmap bitmap;
    /*
      @length - ship length
      @height - ship height
     */
    private float length;
    private float height;
    /* @x - far left of the rectangle which forms ship
       @y - top coordinate
    * */
    private float x;
    private float y;
    // Is the ship moving and in which direction
    private boolean shipMoving = false;
    //Boundaries of screen
    private float leftMargin;
    private float rightMargin;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public Player(Context context, int screenX, int screenY) {

        // Initialize a blank RectF
        rect = new RectF();


        length = screenX / 10;
        height = screenY / 10;

        this.context = context;

        leftMargin = 5;
        rightMargin = screenX - length;
        // Start ship in roughly the screen centre
        x = screenX / 2;
        y = screenY - height;

        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        bitmapSetup();

        // How fast is the spaceship in pixels per second
        speed = 350;
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
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playermove);
            rect.left = x;
            rect.top = y;
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);

        }
        // Update rect which is used to detect hits

        bitmapSetup();
        rect.bottom = y + height;

        rect.right = x + length;
    }

    void bitmapSetup() {
        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);
    }
}
