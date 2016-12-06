package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 *
 * @author krom
 */
public class UIButton implements UIElement
{
    public Bitmap image;
    public Rect src;
    public RectF dst;
    
    public UIButton(final Bitmap image, final Rect src, final float x, final float y)
    {
        this.image = image;
        this.src = src;
        this.dst = new RectF(x, y, x + src.width(), y + src.height());
    }
    
    public void draw(Canvas c)
    {
        c.drawBitmap(image, src, dst, null);
    }
    
}
