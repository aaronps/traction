package dev.aaronps.traction.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import dev.aaronps.traction.GameResources;

/**
 *
 * @author krom
 */
public class UINumber implements UIElement
{
    private static final int NUMBER_WIDTH = 24;
    private static final int NUMBER_HEIGHT = 32;
    private static final int DOT_WIDTH = 16;

    private static final int MAX_DIGITS = 32;
    private static final int[] static_digits = new int[MAX_DIGITS];
    
    private static final Rect number_src = new Rect(0, 0, 24, 32);
    private static final Rect number_dst = new Rect(0, 0, 24, 32);
    
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
        
        final int total_draw_len = total_chars * DOT_WIDTH;

        final Rect src = number_src;
        final Rect dst = number_dst;
        
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

        final Bitmap font = GameResources.numbers_24x32;

        // NOTE: offset by DOT_WIDTH makes numbers nicely packed!!
        
        if ( decimals > 0 )
        {
            do
            {
                src.offsetTo(digits[--digit] * NUMBER_WIDTH, 0);
                c.drawBitmap(font, src, dst, null);
                dst.offset(DOT_WIDTH, 0);
            } while ( digit > decimals );
            
            // draw dot
            src.offsetTo(10 * NUMBER_WIDTH, 0);
            c.drawBitmap(font, src, dst, null);
            dst.offset(DOT_WIDTH / 2, 0);
        }
        
        do
        {
            src.offsetTo(digits[--digit] * NUMBER_WIDTH, 0);
            c.drawBitmap(font, src, dst, null);
            dst.offset(DOT_WIDTH, 0);
        } while (digit > 0);
    }
}
