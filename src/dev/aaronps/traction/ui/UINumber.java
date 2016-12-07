package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import dev.aaronps.traction.Res;

/**
 *
 * @author krom
 */
public class UINumber implements UIElement
{
    
    private static final int MAX_DIGITS = 32;
    private static final int[] static_digits = new int[MAX_DIGITS];
    public int value;
    public int decimals;
    public int x;
    public int y;
    
    /**
     * 0 - left align
     * 1 - center align
     * 2 - right align
     */
    public int align;
    
    public UINumber(final int decimals, final int x, final int y, final int align)
    {
        this.value = 0;
        this.decimals = decimals;
        this.x = x;
        this.y = y;
        this.align = align;
    }
    
    public void draw(Canvas c)
    {
        final int[] digits = static_digits;
        int digit = 0;
        int tmp = value;
        int total_chars = 0;
        
        if ( decimals > 0 )
        {
            total_chars = decimals + 1;
            for ( int n = 0; n < decimals; n++ )
            {
                digits[digit++] = tmp % 10;
                tmp /= 10;
            }
        }
        
        do
        {
            digits[digit++] = tmp % 10;
            tmp /= 10;
            ++total_chars;
        } while (tmp > 0);
        
        final int total_draw_len = total_chars * Res.NUMBER_WIDTH;

        final Rect src = Res.number_src;
        final Rect dst = Res.number_dst;
        
        if ( align == 0 )
        {
            dst.offsetTo(x, y);
        }
        else if ( align == 1 )
        {
            final int soff = x - total_draw_len / 2;
            dst.offsetTo(soff, y);
        }
        else if ( align == 2 )
        {
            dst.offsetTo(x - total_draw_len, y);
        }

        final Bitmap font = Res.numbers_16x32;

        // NOTE: offset by DOT_WIDTH makes numbers nicely packed!!
        
        if ( decimals > 0 )
        {
            do
            {
                src.offsetTo(digits[--digit] * Res.NUMBER_WIDTH, 0);
                c.drawBitmap(font, src, dst, null);
                dst.offset(Res.NUMBER_WIDTH, 0);
            } while ( digit > decimals );
            
            // draw dot
            src.offsetTo(10 * Res.NUMBER_WIDTH + 4, 0);
            c.drawBitmap(font, src, dst, null);
            dst.offset(Res.NUMBER_WIDTH/2, 0);
        }
        
        do
        {
            src.offsetTo(digits[--digit] * Res.NUMBER_WIDTH, 0);
            c.drawBitmap(font, src, dst, null);
            dst.offset(Res.NUMBER_WIDTH, 0);
        } while (digit > 0);
    }
}
