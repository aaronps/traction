package dev.aaronps.traction.gamelayers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import dev.aaronps.traction.Sprite;

public class SpriteLayer implements GameLayer
{
    public Sprite[] sprites;
    public int sprite_count;
    
    public SpriteLayer(final int max_sprites)
    {
        sprite_count = 0;
        sprites = new Sprite[max_sprites];
        for ( int n = 0; n < max_sprites; n++ )
        {
            sprites[n] = new Sprite();
        }
    }
    
    public void reset() { sprite_count = 0; }
    
    public void add(final Bitmap image, final float x, final float y)
    {
        final Sprite s = sprites[sprite_count++];
        s.image = image;
        s.x = x;
        s.y = y;
    }

    @Override
    public void draw( Canvas canvas)
    {
        final Sprite[] ss = sprites;
        final int count = sprite_count;
        for ( int n = 0; n < count; n++ )
        {
            final Sprite s = ss[n];
            canvas.drawBitmap( s.image, s.x, s.y, null);
        }
    }
    
}
