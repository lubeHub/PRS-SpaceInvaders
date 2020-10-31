package com.example.myapplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
/*
@param length Length of the ship
 */
public class Player {
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
    private float  length;
    private float  height;

    /* @x - far left of the rectangle which forms ship
       @y - top coordinate
    * */
    private float x;
    private float y;

    // Which ways can the ship move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = STOPPED;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public Player(Context context, int screenX, int screenY){

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX/10;
        height = screenY/10;

        // Start ship in roughly the screen centre
        x = screenX / 2;
        y = screenY - height;

        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);

        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // How fast is the spaceship in pixels per second
        speed = 350;
    }

    public RectF getRect(){
        return rect;
    }

    // This is a getter method to make the rectangle that
    // defines our ship available in SpaceInvadersView class
    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getHeight(){
        return height;
    }


    public float getLength(){
        return length;
    }

    // This method will be used to change/set if the ship is going left, right or nowhere
    public void setMovementState(int state){
        shipMoving = state;
    }

    // This update method will be called from update in SpaceInvadersView
    // It determines if the player ship needs to move and changes the coordinates
    // contained in x if necessary
    public void update(long fps){
        if(shipMoving == LEFT){
            x = x - speed / fps;
        }

        if(shipMoving == RIGHT){
            x = x + speed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }
}
