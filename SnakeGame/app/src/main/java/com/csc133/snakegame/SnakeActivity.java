package com.csc133.snakegame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SnakeActivity extends Activity {

    // Declare an instance of SnakeGame
    SnakeGame mSnakeGame;
    PauseButtonHandler pauseButtonHandler;



    // Set the game up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Set up the layout
        FrameLayout gameLayout = new FrameLayout(this);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        mSnakeGame = new SnakeGame(this, size);

        // Initialize the pause button handler
        pauseButtonHandler = new PauseButtonHandler(this, mSnakeGame);

        mSnakeGame.setPauseButtonHandler(pauseButtonHandler);

        // Set layout parameters for the pause button to place it at the bottom middle of the screen
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        buttonParams.bottomMargin = 50; // Adjust this value as needed

        // Add the game view to the layout
        gameLayout.addView(mSnakeGame);

        // Add the pause button with the specified layout parameters to the layout
        gameLayout.addView(pauseButtonHandler.getPauseButton(), buttonParams);

        // Set gameLayout as the view of the Activity
        setContentView(gameLayout);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();
    }

}