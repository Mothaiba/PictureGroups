package com.example.lordone.picturegroups.BaseClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Custom Shape Button which ignores touches on transparent background.
 */
public class ButtonWithUntouchableTransparentBg extends Button {

    public ButtonWithUntouchableTransparentBg(Context context) {
        this(context, null);
    }

    public ButtonWithUntouchableTransparentBg(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonWithUntouchableTransparentBg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDrawingCacheEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        // ignores touches on transparent background
        if (isPixelTransparent(x, y))
            return true;
        else
            return super.onTouchEvent(event);
    }

    /**
     * @return true if pixel from (x,y) is transparent
     */
    private boolean isPixelTransparent(int x, int y) {
        Bitmap bmp = Bitmap.createBitmap(getDrawingCache());
        int color = Color.TRANSPARENT;
        try {
            color = bmp.getPixel(x, y);
        } catch (IllegalArgumentException e) {
            // x or y exceed the bitmap's bounds.
            // Reverts the View's internal state from a previously set "pressed" state.
            setPressed(false);
        }

        // Ignores touches on transparent background.
        if (color == Color.TRANSPARENT)
            return true;
        else
            return false;
    }
}