/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 *
 * @author krom
 */
public class UIImage implements UIElement
{
    Bitmap image = null;
    float x = 0;
    float y = 0;
    
    public UIImage()
    {
        
    }
    
    public UIImage(final Bitmap image, final float x, final float y)
    {
        this.image = image;
        this.x = x;
        this.y = y;
    }
    
    void set(final Bitmap image, final float x, final float y)
    {
        this.image = image;
        this.x = x;
        this.y = y;
    }

    public void draw(final Canvas c)
    {
        c.drawBitmap(image, x, y, null);
    }
}
