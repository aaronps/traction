package dev.aaronps.gameengine;

import android.graphics.Canvas;

/**
 *
 * @author krom
 */
public interface Screen
{
    public void enter();
    public Screen logic(final float time);
    public void interpol(final float time);
    public void drawGame(final Canvas c);
    public void drawUI(final Canvas c);
}
