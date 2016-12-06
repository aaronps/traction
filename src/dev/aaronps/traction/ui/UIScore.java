package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import dev.aaronps.traction.Res;

/**
 *
 * @author krom
 */
public class UIScore implements UIElement
{
    private final int[] date = new int[]
    {
        2,
        0,
        0,
        0,
        11, // '-'
        0,
        0,
        11,
        0,
        0
    };
    
    private final int[] score = new int[8];
    private int scorelen = 0;
    public int x = 0;
    public int y = 0;
    public int w = 0;
    
    public UIScore()
    {
        
    }
    
    public final void setPosition(final int x, final int y, final int w)
    {
        this.x = x;
        this.y = y;
        this.w = w;
    }
    
    public final void set(final String scorestr)
    {
        date[2] = scorestr.charAt(2)-0x30;
        date[3] = scorestr.charAt(3)-0x30;
        date[5] = scorestr.charAt(5)-0x30;
        date[6] = scorestr.charAt(6)-0x30;
        date[8] = scorestr.charAt(8)-0x30;
        date[9] = scorestr.charAt(9)-0x30;
        
        scorelen = 0;
        final int e = scorestr.length();
        for ( int n = 10; n < e; n++ )
        {
            final char c = scorestr.charAt(n);
            if ( c == '.' )
            {
                score[scorelen++] = 10; //'.'
            }
            else
            {
                score[scorelen++] = c - 0x30;
            }
        }
    }
    
    public void draw(Canvas c)
    {
        final Rect src = Res.number_src;
        final Rect dst = Res.number_dst;
        final Bitmap font = Res.numbers_16x24;
        
        dst.offsetTo(x, y);
        for ( int n = 0; n < 10; n++ )
        {
            src.offsetTo(date[n] * Res.NUMBER_WIDTH, 0);
            c.drawBitmap(font, src, dst, null);
            dst.offset(Res.NUMBER_WIDTH, 0);
        }
        
        dst.offsetTo(x + w - scorelen * Res.NUMBER_WIDTH + Res.NUMBER_WIDTH /2, y);
        for ( int n = 0; n < scorelen; n++ )
        {
            final int v = score[n];
            if ( v < 10 )
            {
                src.offsetTo(v * Res.NUMBER_WIDTH, 0);
                c.drawBitmap(font, src, dst, null);
                dst.offset(Res.NUMBER_WIDTH, 0);
            }
            else
            {
                src.offsetTo(10 * Res.NUMBER_WIDTH + 4, 0);
                c.drawBitmap(font, src, dst, null);
                dst.offset(Res.NUMBER_WIDTH/2, 0);
            }
        }
    }
    
}
