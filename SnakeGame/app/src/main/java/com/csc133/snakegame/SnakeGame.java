package com.csc133.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.app.Activity;


class SnakeGame extends SurfaceView implements Runnable, GameControls{


    private static final int SCORE_TEXT_SIZE = 40;
    private static final int SCORE_MARGIN_LEFT = 20;
    private static final int SCORE_MARGIN_TOP = 120;
    private Activity mActivity;

    private Typeface gameFont;
    private Bitmap mBackgroundBitmap;
    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private PauseButtonHandler pauseButtonHandler;


    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    // A snake ssss
//    private Snake mSnake;
////    // And an apple
//    private Apple mApple;


    // DrawableMovable interfaces for the snake and apple
    private DrawableMovable mSnake;
    private DrawableMovable mApple;



    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);

        // Set the activity context
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }

        // Calculate the block size and number of blocks high based on the screen size
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;

        // Initialize SoundPool with appropriate settings based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        // Load sound effects from assets
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error handling
        }

        // Initialize SurfaceHolder and Paint objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Load background bitmap and resize it to fit the screen
        mBackgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_background);
        mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, size.x, size.y, false);

        // Load custom font
        gameFont = Typeface.createFromAsset(context.getAssets(), "fonts/press_start_2p.ttf");

        // Initialize Apple and Snake objects
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
    }


    public void setPauseButtonHandler(PauseButtonHandler handler) {
        this.pauseButtonHandler = handler;
    }

    // Called to start a new game
    public void newGame() {
        // Reset the game objects
        mSnake.reset();
        mApple.reset();

        // Prepare the game objects for a new game
        ((Apple)mApple).spawn(); // Note: spawn is specific to Apple

        // Reset the score
        mScore = 0;

        // Setup mNextFrameTime so an update can be triggered
        mNextFrameTime = System.currentTimeMillis();

        // Reset the pause button if the handler is set
        if (pauseButtonHandler != null) {
            pauseButtonHandler.resetPauseButton();
        }
    }



    // Handles the game loop
    @Override
    public void run() {
        // Ensure newGame is called before game loop starts
        newGame();
        while (mPlaying) {
            if(!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }

    // Implement the GameControls interface methods
    @Override
    public void pauseGame() {
        mPaused = true;
    }

    @Override
    public void resumeGame() {
        mPaused = false;
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    public void update() {
        mSnake.move(); // Move the snake

        // Check if the snake has eaten an apple
        if (((Snake)mSnake).checkDinner(mApple.getLocation())) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // UI updates here
                    ((Apple)mApple).spawn(); // Respawn the apple
                    mScore += 1; // Increase the score
                }
            });
            mSP.play(mEat_ID, 1, 1, 0, 0, 1); // Play eating sound
        }

        // Check if the snake has died
        if (((Snake)mSnake).detectDeath()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Don't automatically start a new game. Just pause and show "Tap to Play".
                    mPaused = true;

                }
            });
            mSP.play(mCrashID, 1, 1, 0, 0, 1); // Play death sound
        }
    }


    // Do all the drawing
    // Original lengthy draw() method
    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            if (mCanvas != null) {
                // Draw background
                drawBackground();

                // Draw names and score
                drawText();

                // Draw game objects
                drawGameObjects();

                // If game is paused, draw "Tap to Play" message
                if (mPaused) {
                    drawTapToPlay();
                }

                // Unlock canvas and post
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    // Refactored drawBackground() method
    private void drawBackground() {
        mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
    }

    // Refactored drawText() method
    private void drawText() {
        // Draw names "Jacob & Adiba" in top right corner
        drawNames();

        // Draw score in top left corner
        drawScore();
    }

    // Refactored drawNames() method
    private void drawNames() {
        mPaint.setTextSize(40);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(gameFont);
        String names = "Jacob & Adiba";
        float textWidth = mPaint.measureText(names);
        float xPositionNames = mCanvas.getWidth() - textWidth - 20; // 20 pixels from right edge
        float yPositionNames = 60; // 60 pixels from top
        mCanvas.drawText(names, xPositionNames, yPositionNames, mPaint);
    }

    // Refactored drawScore() method
    private void drawScore() {
        mPaint.setTextSize(60);
        mCanvas.drawText("Score: " + mScore, 20, 120, mPaint);
    }

    // Refactored drawGameObjects() method
    private void drawGameObjects() {
        // Draw apple and snake
        mApple.draw(mCanvas, mPaint);
        mSnake.draw(mCanvas, mPaint);
    }

    // Refactored drawTapToPlay() method
    private void drawTapToPlay() {
        mPaint.setTextSize(90);
        float tapToPlayWidth = mPaint.measureText("Tap to Play");
        float xPositionTapToPlay = (mCanvas.getWidth() - tapToPlayWidth) / 2;
        float yPositionTapToPlay = mCanvas.getHeight() / 2;
        mCanvas.drawText("Tap to Play", xPositionTapToPlay, yPositionTapToPlay, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused) {
                    // Check if the game was paused due to the snake's death and waiting for a restart
                    newGame(); // Start a new game
                    mPaused = false; // Unpause the game
                    // No need to check mPlaying or start a new thread here as newGame() and resume() should handle it
                    resume(); // Ensure the game resumes correctly if it was not already playing
                    return true;
                } else {
                    // If the game is already playing, handle snake direction changes
                    ((Snake)mSnake).switchHeading(motionEvent);
                }
                break;
            default:
                break;
        }
        return true;
    }



    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    // Start the thread
    public void resume() {
        newGame(); // Set up the initial game state
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }

    // Called to start a new game

}
