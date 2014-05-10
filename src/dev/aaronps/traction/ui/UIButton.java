package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 *
 * @author krom
 */
public class UIButton implements UIElement
{
    public Bitmap image;
    public RectF rect;
    public Paint paint;
    
    public UIButton(final Bitmap image, final float x, final float y, final Paint paint)
    {
        this.image = image;
        this.paint = paint;
        rect = new RectF(x, y, x + image.getWidth(), y + image.getHeight());
    }
    
    public void draw(Canvas c)
    {
        c.drawRect(rect, paint);
        c.drawBitmap(image, rect.left, rect.top, null);
    }
    
}
