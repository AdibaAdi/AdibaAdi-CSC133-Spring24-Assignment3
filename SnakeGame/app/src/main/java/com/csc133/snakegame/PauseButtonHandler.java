package com.csc133.snakegame;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class PauseButtonHandler implements View.OnClickListener {
    private final GameControls gameControls;
    private boolean isPaused = false;
    private Button pauseButton;

    public PauseButtonHandler(Context context, GameControls gameControls) {
        this.gameControls = gameControls;

        // Create and set up the pause button
        this.pauseButton = new Button(context);
        pauseButton.setText("Pause");
        pauseButton.setOnClickListener(this);
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    @Override
    public void onClick(View view) {
        if (isPaused) {
            gameControls.resumeGame();
            ((Button)view).setText("Pause"); // Change text back to "Pause"
            isPaused = false;
        } else {
            gameControls.pauseGame();
            ((Button)view).setText("Resume"); // Change text to "Resume"
            isPaused = true;
        }
    }
    public void resetPauseButton() {
        pauseButton.setText("Pause");
        isPaused = false;
    }

}