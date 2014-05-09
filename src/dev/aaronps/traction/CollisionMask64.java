package dev.aaronps.traction;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CollisionMask64
{
    private int bm_width = 0;
    private int bm_height = 0;
    private long[] mask = null;

    public CollisionMask64()
    {
        // empty
    }

    public final int getWidth()
    {
        return bm_width;
    }

    public final int getHeight()
    {
        return bm_height;
    }

    public final void fromBitmap(final Bitmap bitmap)
    {
        bm_width = bitmap.getWidth();
        bm_height = bitmap.getHeight();

        if (bm_width > 64 || bm_height > 64)
        {
            throw new RuntimeException("Bitmap bigger than supported, max 64x64, bitmap is " + bm_width + "x" + bm_height);
        }

        mask = new long[bm_height];

        for (int y = 0; y < bm_height; y++)
        {
            long m = 1L << 63;
            for (int x = 0; x < bm_width; x++)
            {
                if (Color.alpha(bitmap.getPixel(x, y)) > 31)
                {
                    mask[y] |= m;
                }

                m >>>= 1;
            }
        }
    }

    public final boolean collidesWith(  final int x, int y, int h,
                                        final CollisionMask64 other,
                                        final int ox, int oy )
    {
        final long[] omask = other.mask;
        do
        {
            if (((mask[y] << x) & (omask[oy] << ox)) != 0)
            {
                return true;
            }

            y++;
            oy++;
        } while ( --h > 0 );

        return false;
    }

}
