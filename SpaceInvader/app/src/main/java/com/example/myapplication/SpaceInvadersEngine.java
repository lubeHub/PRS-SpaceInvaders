package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

@SuppressLint("ViewConstructor")
public class SpaceInvadersEngine extends SurfaceView implements Runnable {

    Context context;
    //List of background images
    ArrayList<Background> backgrounds;
    // Up to 60 invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    //The background
    Background bg;
    // The score
    int score = 0;
    float xDelta;
    float yDelta;
    // This is our thread
    private Thread gameThread = null;
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
    // The size of the screen in pixels
    private int screenX;
    private int screenY;
    //screen margins
    private float top;
    private float left;
    private float right;
    private float bottom;
    // The players ship
    private Player playerShip;
    // The player's bullets
    private Bullet[] playerBullets = new Bullet[200];
    private int maxPlayerBullet = 200;
    private int playerNextBullet;
    //Counter used to delay between  player's bullets
    private int shootCounter;
    // The invaders bullets
    private Bullet[] invadersBullets = new Bullet[maxPlayerBullet];
    private int nextBullet;
    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int uhID = -1;
    private int ohID = -1;
    //The image used for live
    private Bitmap heart;
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

        addBackgroundsToList(screenX, screenY);
        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);


            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);
/*
            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);*/

        } catch (IOException e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
        top = 5;
        left = 5;
        right = x - playerShip.getLength();
        bottom = y - playerShip.getHeight();

    }
Random random = new Random();
    private void prepareLevel() {
        // Here we will initialize all the game objects
        // Make a new player space ship
        playerShip = new Player(context, screenX, screenY);

        // Prepare the player's bullet
        for (int i = 0; i < playerBullets.length; i++) {
            playerBullets[i] = new Bullet(screenY);
        }
        // Initialize the invadersBullets array
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        // Build an army of invaders
        numInvaders = 0;
        for (int column = 0; column < 6; column++) {
            for (int row = 0; row < 5; row++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY,random.nextInt(9)+1);
                numInvaders++;
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
            // This is used to help calculate the fps
            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // We will do something new here towards the end of the project
            // Play a sound based on the menace level
           /* if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // Play Uh
                     //   soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // Play Oh
                   //     soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            } */

        }

    }

    private void update() {

        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;


        // Move the player's ship
        playerShip.update(fps);

        // Update the invaders if visible
        for (int i = 0; i < numInvaders; i++) {

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
                        int maxInvaderBullets = 10;
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
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus()) {
                bullet.update(fps);
            }
        }

        // Update all the invaders bullets if active
        for (Bullet bullet : invadersBullets) {
            if (bullet.getStatus()) {
                bullet.update(fps);
            }
        }

        // Did an invader bump into the edge of the screen
        if (bumped) {

            // Move all the invaders down and change direction
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                // Have the invaders landed
                if (invaders[i].getY() > screenY - screenY / 10) {
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if (lost) {
            prepareLevel();
        }

        // Has the player's bullet hit the top of the screen
        for (Bullet bullet : playerBullets) {
            if (bullet.getImpactPointY() < 0) {
                bullet.setInactive();
            }
        }
        // Has an invaders bullet hit the bottom of the screen
        for (Bullet bullet : invadersBullets) {
            if (bullet.getImpactPointY() > screenY) {
                bullet.setInactive();
            }
        }

        // Has the player's bullet hit an invader
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus()) {
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                            invaders[i].setInvisible();
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            bullet.setInactive();
                            score = score + 10;

                            // Has the player won
                            if (score == numInvaders * 10) {
                                newGameSetup();
                            }
                        }
                    }
                }
            }
        }
        //if playerShip hit invader
        for (int i = 0; i < numInvaders; i++) {
            if (invaders[i].getVisibility()) {
                if (RectF.intersects(playerShip.getRect(), invaders[i].getRect())) {
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    newGameSetup();
                }
            }
        }


        // Has an invader bullet hit the player ship
        for (Bullet bullet : invadersBullets) {
            if (bullet.getStatus()) {
                if (RectF.intersects(playerShip.getRect(), bullet.getRect())) {
                    bullet.setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if (lives == 0) {
                        newGameSetup();
                    }
                }
            }
        }
        for (Background bg : backgrounds) {
            bg.update(fps);
        }
        shootPlayerBullets();
    }

    private void draw() {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

             for(int i=0;i<3;i++) {
                drawBackground(i);
                }
            // Choose the brush color for drawing
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), playerShip.getY(), paint);

            // Draw the invaders
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                }
            }

            // Draw the players bullet if active
            for (Bullet bullet : playerBullets) {
                if (bullet.getStatus()) {
                    canvas.drawRect(bullet.getRect(), paint);
                }
            }
            // Draw the invaders bullets
            for (Bullet bullet : invadersBullets) {
                if (bullet.getStatus()) {
                    canvas.drawRect(bullet.getRect(), paint);
                }
            }

            // Draw the score
            Typeface typeface = ResourcesCompat.getFont(context, R.font.ka1);
            paint.setTypeface(typeface);
            paint.setTextSize(35);
            paint.setColor(Color.WHITE);
            canvas.drawText(String.format("%06d", score), 20, 50, paint);

            //Draw remaining lives
            for (int i = 0; i < lives; i++) {
                canvas.drawBitmap(heart, 220 + i * 50, 23, null);
            }

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    //When player touch the screen
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                xDelta = x - playerShip.getX();
                yDelta = y - playerShip.getY();
                //Player has dragged the finger
            case MotionEvent.ACTION_MOVE:
                if ((x - xDelta) >= left && (x - xDelta) <= right && (y - yDelta) >= top && (y - yDelta) <= bottom) {
                    playerShip.setX(x - xDelta);
                    playerShip.setY(y - yDelta);
                    paused = false;
                }
                playerShip.setMovementState(true);
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                playerShip.setMovementState(false);
                break;
        }
        return true;
    }

    //Drawing background from a list of backgrounds
    private void drawBackground(int position) {

        // Make a copy of the relevant background
        bg = backgrounds.get(position);

        // define what portion of images to capture and
        // what coordinates of screen to draw them at

        // For the regular bitmap
        Rect fromRect1 = new Rect(0, 0, bg.width, bg.height - bg.yClip);
        Rect toRect1 = new Rect(bg.startX, bg.yClip, bg.endX, bg.height);

        // For the reversed background
        Rect fromRect2 = new Rect(0, bg.height - bg.yClip, bg.width, bg.height);
        Rect toRect2 = new Rect(bg.startX, 0, bg.endX, bg.yClip);

        //draw the two background bitmaps
        canvas.drawBitmap(bg.bitmap, toRect1, fromRect1, paint);
        canvas.drawBitmap(bg.bitmap, toRect2, fromRect2, paint);


    }

    //Adding background images to list
    private void addBackgroundsToList(int screenWidth, int screenHeight) {
        backgrounds = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            backgrounds.add(new Background(
                    this.context,
                    screenWidth,
                    screenHeight,
                    "bkgd_" + i, 0, 110, 100));
        }
    }

    //Player shooting method
    private void shootPlayerBullets() {
        if (shootCounter < 14) {
            shootCounter++;
        } else {
            if (playerBullets[playerNextBullet].shoot(playerShip.getX() +
                    playerShip.getLength() / 2, playerShip.getY(), playerBullets[playerNextBullet].UP)) {
                soundPool.play(shootID, 1, 1, 0, 0, 1);
                // Shot fired
                // Prepare for the next shot
                playerNextBullet++;

                // Loop back to the first one if we have reached the last
                if (playerNextBullet == maxPlayerBullet) {
                    // This stops the firing of another bullet until one completes its journey
                    // Because if bullet 0 is still active shoot returns false.
                    playerNextBullet = 0;
                }
            }
            shootCounter = 0;
        }
    }

    private void newGameSetup() {
        paused = true;
        lives = 3;
        score = 0;
        prepareLevel();

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