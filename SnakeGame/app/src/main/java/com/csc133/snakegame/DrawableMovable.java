package com.csc133.snakegame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;


public interface DrawableMovable {
    void draw(Canvas canvas, Paint paint);
    void move();
    Point getLocation();
    void reset();

}
