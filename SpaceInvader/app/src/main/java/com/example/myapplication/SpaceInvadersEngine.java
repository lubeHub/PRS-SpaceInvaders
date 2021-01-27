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
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.util.ArrayList;


import static java.lang.String.*;


@SuppressLint("ViewConstructor")
public class SpaceInvadersEngine extends SurfaceView implements Runnable {

    private final Context context;
    //List of background images
    private ArrayList<Background> backgrounds;
    // Up to 60 invaders
    private final Invader[] invaders = new Invader[60];
    private int numInvaders = 0;
    // The score
    private int score = 0;
    private float xDelta;
    private float yDelta;
    // This is our thread
    private Thread drawThread = null;
    private Thread gameThread = null;

    // Our SurfaceHolder to lock the surface before we draw our graphics
    private final SurfaceHolder ourHolder;
    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;
    // fGame is paused at the start
    private boolean paused = true;
    // A Canvas and a Paint object
    private Canvas canvas;
    private final Paint paint;
    // This variable tracks the game frame rate
    private long fps;
    // The size of the screen in pixels
    private final int screenX;
    private final int screenY;
    //screen margins
    private final float top;
    private final float left;
    private final float right;
    private final float bottom;
    // The players ship
    private Player playerShip;
    // The player's bullets
    private final int maxPlayerBullet = 200;
    private final Bullet[] playerBullets = new Bullet[maxPlayerBullet];

    private int playerNextBullet;
    //Counter used to delay between  player's bullets
    private int shootCounter=30;
    // The invaders bullets
    private final Bullet[] invadersBullets = new Bullet[maxPlayerBullet];
    private int nextBullet;
    // For sound FX
    private final SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    //number of invaders in column and row
    private int columnNumber = 3;
    private int rowNumber = 3;
    //The image used for live
    private Bitmap heart;
    private Bitmap bannerLoading;
    // Lives
    private int lives = 3;
    // How menacing (fast) should the sound be?
    private long menaceInterval = 1000;
    private int previousScore = 0;
    private int currentLevel = 1;
    private int numUpgrades = 0;
    private int collectedUpgrades=1;
    private int bulletDamage = 100;
    private final int baseScore = 100;
    private final int baseDamage = 100;
    private final Upgrade[] upgrade = new Upgrade[10];



    // When the we initialize (call new()) on gameView
    // This special constructor method runs
    public SpaceInvadersEngine(Context context, int x, int y) {

        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.
        super(context);

        // Make a globally available copy of the context so we can use it in another method
        this.context = context;

        ImageView image = new ImageView(context);
        image.setImageResource(R.drawable.ship5);

        bannerLoading= BitmapFactory.decodeResource(context.getResources(),R.drawable.loading_banner);
        bannerLoading = Bitmap.createScaledBitmap(bannerLoading,
                x,
                y/7,
                false);
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


        } catch (IOException e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }


        prepareLevel(currentLevel);

        top = 5;
        left = 5;
        right = x - playerShip.getLength();
        bottom = y - playerShip.getHeight();

    }




    @Override
    public void run() {
        while (playing) {


            // Draw the frame
            draw();


        }
    }

    private void update() {

        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;


        // Move the player's ship
        playerShip.update(fps);
        shootPlayerBullets();

        // Update the invaders if visible
        for (int i = 0; i < numInvaders; i++) {

            if (invaders[i].getVisibility()) {
                // Move the next invader
                invaders[i].update(fps);

                // Does he want to take a shot?
                if (invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength(), invaders[i].getType())) {

                    // If so try and spawn a bullet
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2 - screenX/120,
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
        //Update all upgrades if active
        for (Upgrade upgrades : upgrade) {
            if (upgrades.getStatus()) {
                upgrades.update(fps);
            }
        }

        // Did an invader bump into the edge of the screen
        if (bumped) {

            // Move all the invaders down and change direction
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                // Have the invaders landed
                if (invaders[i].getY() > screenY - (float) screenY / 10) {
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if (lost) {
            currentLevel = 1;
            prepareLevel(currentLevel);
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
        //Has an upgrade hit the bottom of the screen
        for (Upgrade upgrades : upgrade) {
            if (upgrades.getImpactPointY() > screenY) {
                upgrades.setInactive();
            }
        }
        // Has the player's bullet hit an invader
        for (Bullet bullet : playerBullets) {
            if (bullet.getStatus()) {
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                            invaders[i].setHealth(invaders[i].getHealth() - bullet.getBulletDamage());
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            bullet.setInactive();
                            if (invaders[i].getHealth() <= 0) {
                                invaders[i].setInvisible();
                                score = score + invaders[i].getType() * baseScore;

                                if (invaders[i].dropUpgradeChance()) {
                                    upgrade[numUpgrades].dropUpgrade(invaders[i].getX() + invaders[i].getLength() / 2, invaders[i].getY());
                                    numUpgrades++;
                                }
                            }
                            // Has the player won

                            if (score == previousScore + numInvaders * invaders[i].getType() * baseScore) {
                                currentLevel++;
                                previousScore = score;
                                prepareLevel(currentLevel);
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
                    currentLevel = 1;
                    prepareLevel(currentLevel);              }
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
                        currentLevel = 1;

                       prepareLevel(currentLevel);
                    }
                }
            }
        }
        //if player catch an upgrade
        for (Upgrade upgrades : upgrade) {
            if (upgrades.getStatus()) {
                if (RectF.intersects(upgrades.getRect(), playerShip.getRect())) {
                    if(collectedUpgrades<5)
                        collectedUpgrades += 2;
                    else
                    {
                    for (Bullet bullets : playerBullets) {

                        bulletDamage = bullets.getBulletDamage() + baseDamage;
                        bullets.setBulletDamage(bulletDamage);
                        }
                    }
                    upgrades.setInactive();
                }
            }
        }
        for (Background bg : backgrounds) {
            bg.update(fps);
        }
    }

    @SuppressLint("DefaultLocale")
    private void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            for (int i = 0; i < 3; i++) {
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
                    canvas.drawBitmap(bullet.getBitmap(),bullet.getX(),bullet.getY(), paint);
                }
            }
            // Draw the invaders bullets
            for (Bullet bullet : invadersBullets) {
                if (bullet.getStatus()) {
                    canvas.drawBitmap(bullet.getBitmap(),bullet.getX(),bullet.getY(), paint);
                }
            }
            for (Upgrade upgrades : upgrade) {
                if (upgrades.getStatus()) {
                    canvas.drawBitmap(upgrades.getBitmap(), upgrades.getX(), upgrades.getY(), paint);
                }
            }

            // Draw the score
            Typeface typeface = ResourcesCompat.getFont(context, R.font.ka1);
            paint.setTypeface(typeface);
            paint.setTextSize(35);
            paint.setColor(Color.WHITE);
            canvas.drawText(format("%06d", score), 20, 50, paint);

            //Draw remaining lives
            for (int i = 0; i < lives; i++) {
                canvas.drawBitmap(heart, 220 + i * 50, 23, null);
            }
           // canvas.drawBitmap(bannerLoading,0,(float)(screenY/2-(screenY/14)),null);
            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }

    }

    //When player touch the screen
    @SuppressLint("ClickableViewAccessibility")
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
        //The background
        Background bg = backgrounds.get(position);

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
        if (shootCounter < 30) {
            shootCounter++;
        }
        else {
            switch(collectedUpgrades)
            {
                case 1:
                    if (playerBullets[playerNextBullet].shoot(playerShip.getX() +
                            playerShip.getLength() / 2 - screenX/120, playerShip.getY(), playerBullets[playerNextBullet].UP)) {
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
                    break;
                case 3:
                    for(int i=1;i<=3;i++) {

                        if (playerBullets[playerNextBullet].shoot(playerShip.getX() +
                                playerShip.getLength() * i / 4 - screenX/120, playerShip.getY(), playerBullets[playerNextBullet].UP)) {
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
                    }
                    break;
                case 5:
                    for(int i=0;i<5;i++) {

                        if (playerBullets[playerNextBullet].shoot(playerShip.getX() +
                                playerShip.getLength()*i / 4 - screenX/120, playerShip.getY(), playerBullets[playerNextBullet].UP)) {
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
                    }
                    break;
            }
          /*  for(int i=1;i<=collectedUpgrades;i++)
            {
            if (playerBullets[playerNextBullet].shoot(playerShip.getX() +
                    playerShip.getLength() / i, playerShip.getY(), playerBullets[playerNextBullet].UP)) {
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

           */
            shootCounter = 0;
        }
        }


    private void prepareLevel(int level) {


        // Here we will initialize all the game objects
        if (level == 11) {
            gameFinished();
        }
        else if (level==10)
        {
            bossInvader();
        }
        else if (level == 1) {

            lives = 3;
            score = 0;
            previousScore = 0;
            bulletDamage = 100;
            playerShip = new Player(context, screenX, screenY,level);
            columnNumber = 3;
            rowNumber = 3;
        }

        paused = true;
        // Prepare the player's bullet
        for (int i = 0; i < playerBullets.length; i++) {
            playerBullets[i] = new Bullet(screenY, screenX, context);
            playerBullets[i].setBulletDamage(bulletDamage);
            playerBullets[i].setBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.bullet),(int)(playerBullets[i].getWidth()),(int)(playerBullets[i].getHeight()),false));
        }
        // Initialize the invadersBullets array
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY,screenX,context);
            invadersBullets[i].setBulletSpeed(350 + (level - 1) * 50);
            invadersBullets[i].setBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.bullet_invader),(int)(invadersBullets[i].getWidth()),(int)(invadersBullets[i].getHeight()),false));
        }
        //Initialize the upgrade array
        for (int i = 0; i < upgrade.length; i++) {
            upgrade[i] = new Upgrade(context, screenY);
        }
        // Build an army of invaders
        numInvaders = 0;
        for (int column = 0; column < columnNumber; column++) {
            for (int row = 0; row < rowNumber; row++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY, level);
                numInvaders++;
            }
        }
        if (columnNumber < 7) {
            columnNumber++;
        } else {
            rowNumber++;
        }
        playerShip=new Player(context, screenX, screenY,level);
        // Reset the menace level
        menaceInterval = 1000;
      
    }

    private void gameFinished() {

    }
    private void bossInvader(){

    }
    public void pause() {
        playing = false;
        try {
            gameThread.join();
            drawThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SpaceInvadersActivity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(runnable);
        gameThread.start();
        drawThread = new Thread(this);
        drawThread.start();

    }



    Runnable runnable = new Runnable() {
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

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                // This is used to help calculate the fps
                long timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        };


}