package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.fonts.Font;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class SpaceInvadersEngine extends SurfaceView implements Runnable{

    Context context;

    // This is our thread
    private Thread gameThread = null;
    ArrayList<Background> backgrounds;

    // Our SurfaceHolder to lock the surface before we draw our graphics
    private SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // fGame is paused at the start
    private boolean paused = true;

    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    // The size of the screen in pixels
    private int screenX;
    private int screenY;

    // The players ship
    private Player playerShip;

    // The player's bullet
    private Bullet[] bullet = new Bullet[200];

    // The invaders bullets
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    // Up to 60 invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;
    private Bitmap backgroundImage;
    private Bitmap heart;

    Background bg;
    // The score
    int score = 0;

    // Lives
    private int lives = 3;

    // How menacing (fast) should the sound be?
    private long menaceInterval = 1000;
    // Which menace sound should play next
    private boolean uhOrOh;
    // When did we last play a menacing sound
    private long lastMenaceTime = System.currentTimeMillis();

    // When the we initialize (call new()) on gameView
    // This special constructor method runs
    public SpaceInvadersEngine(Context context, int x, int y) {

        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.
        super(context);

        // Make a globally available copy of the context so we can use it in another method
        this.context = context;
        backgroundImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        heart = Bitmap.createScaledBitmap(heart,
                42,
                42,
                false);
        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;
        addBackgroundsToList(screenX,screenY);
        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel(){

        // Here we will initialize all the game objects

        // Make a new player space ship
        playerShip = new Player(context, screenX, screenY);

        // Prepare the player's bullet
        for(int i=0;i<bullet.length;i++) {
            bullet[i] = new Bullet(screenY);
        }
        // Initialize the invadersBullets array
        for(int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);
        }

        // Build an army of invaders
        numInvaders = 0;
        for(int column = 0; column < 6; column ++ ){
            for(int row = 0; row < 5; row ++ ){
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders ++;
            }
        }

        // Reset the menace level
        menaceInterval = 1000;


    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();
            // Update the frame
            if (!paused) {
                update();
            }
            // Draw the frame
            draw();
            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // We will do something new here towards the end of the project
            // Play a sound based on the menace level
            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // Play Uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // Play Oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }

        }

    }

    private void update(){

        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;



        // Move the player's ship
        playerShip.update(fps);

        // Update the invaders if visible
        for(int i = 0; i < numInvaders; i++) {

            if (invaders[i].getVisibility()) {
                // Move the next invader
                invaders[i].update(fps);

                // Does he want to take a shot?
                if (invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength())) {

                    // If so try and spawn a bullet
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), invadersBullets[i].DOWN)) {

                        // Shot fired
                        // Prepare for the next shot
                        nextBullet++;

                        // Loop back to the first one if we have reached the last
                        if (nextBullet == maxInvaderBullets) {
                            // This stops the firing of another bullet until one completes its journey
                            // Because if bullet 0 is still active shoot returns false.
                            nextBullet = 0;
                        }
                    }
                }

                // If that move caused them to bump the screen change bumped to true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0) {

                    bumped = true;

                }
            }

        }

        // Update the players bullet
        for(int i=0;i<bullet.length;i++) {
            if (bullet[i].getStatus()) {
                bullet[i].update(fps);
            }
        }

        // Update all the invaders bullets if active
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // Did an invader bump into the edge of the screen
        if(bumped){

            // Move all the invaders down and change direction
            for(int i = 0; i < numInvaders; i++){
                invaders[i].dropDownAndReverse();
                // Have the invaders landed
                if(invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if(lost){
            prepareLevel();
        }

        // Has the player's bullet hit the top of the screen
        for(int i = 0; i < bullet.length; i++) {
            if (bullet[i].getImpactPointY() < 0) {
                bullet[i].setInactive();
            }
        }
        // Has an invaders bullet hit the bottom of the screen
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getImpactPointY() > screenY){
                invadersBullets[i].setInactive();
            }
        }

        // Has the player's bullet hit an invader
        for(int j=0;j<bullet.length;j++) {
            if (bullet[j].getStatus()) {
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (RectF.intersects(bullet[j].getRect(), invaders[i].getRect())) {
                            invaders[i].setInvisible();
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            bullet[j].setInactive();
                            score = score + 10;

                            // Has the player won
                            if (score == numInvaders * 10) {
                                paused = true;
                                score = 0;
                                lives = 3;
                                prepareLevel();
                            }
                        }
                    }
                }
            }
        }





        // Has an invader bullet hit the player ship
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())){
                    invadersBullets[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if(lives == 0){
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }
        for (Background bg : backgrounds) {
            bg.update(fps);
        }
    }

    private void draw(){
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();


            // Draw the background image
         //   canvas.drawBitmap(backgroundImage,0,0,null);
            for(int i=0;i<backgrounds.size();i++) {
                drawBackground(i);
            }


            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - playerShip.getHeight(), paint);

            // Draw the invaders
            for(int i = 0; i < numInvaders; i++){
                if(invaders[i].getVisibility()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    }else{
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }

            // Draw the players bullet if active
            for(int i=0;i<bullet.length;i++) {
                if (bullet[i].getStatus()) {
                    canvas.drawRect(bullet[i].getRect(), paint);
                }
            }
            // Draw the invaders bullets
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // Draw the score
            Typeface typeface = ResourcesCompat.getFont(context, R.font.ka1);
            paint.setTypeface(typeface);
            paint.setTextSize(35);
            paint.setColor(Color.WHITE);
            canvas.drawText(String.format("%06d", score), 20,50, paint);

            //Draw remaining lives
            for(int i=0;i<lives;i++) {
                canvas.drawBitmap(heart, 220+i*50, 23, null);
            }

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }


    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if(motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }

                if(motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                    for(int i=0;i<bullet.length;i++)
                    if(bullet[i].shoot(playerShip.getX()+
                            playerShip.getLength()/2,screenY,bullet[i].UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                if(motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break;
        }
        return true;
    }

    private void drawBackground(int position) {

        // Make a copy of the relevant background
         bg = backgrounds.get(position);

        // define what portion of images to capture and
        // what coordinates of screen to draw them at

        // For the regular bitmap
        Rect fromRect1 = new Rect(0, 0, bg.width - bg.xClip, bg.height);
        Rect toRect1 = new Rect(bg.xClip, bg.startY, bg.width, bg.endY);

        // For the reversed background
        Rect fromRect2 = new Rect(bg.width - bg.xClip, 0, bg.width, bg.height);
        Rect toRect2 = new Rect(0, bg.startY, bg.xClip, bg.endY);

        //draw the two background bitmaps
        if (!bg.reversedFirst) {
            canvas.drawBitmap(bg.bitmap, fromRect1, toRect1, paint);
            canvas.drawBitmap(bg.bitmapReversed, fromRect2, toRect2, paint);
        } else {
            canvas.drawBitmap(bg.bitmap, fromRect2, toRect2, paint);
            canvas.drawBitmap(bg.bitmapReversed, fromRect1, toRect1, paint);
        }

    }
        // If SpaceInvadersActivity is paused/stopped
    // shutdown our thread.

    private void addBackgroundsToList(int screenWidth,int screenHeight) {
        backgrounds = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            backgrounds.add(new Background(
                    this.context,
                    screenWidth,
                    screenHeight,
                    "bkgd_" + i, 0, 110, 50));
        }
    }
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SpaceInvadersActivity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

}