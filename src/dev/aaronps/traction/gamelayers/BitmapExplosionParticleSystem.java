package dev.aaronps.traction.gamelayers;

import dev.aaronps.traction.Res;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class BitmapExplosionParticleSystem
{
    // if image changes, update here
    static final int frame_w = 64;
    static final int frame_h = 64;
    static final int image_w = 512;
    static final int image_h = 256;
    static final Rect size_rect = new Rect(0, 0, frame_w, frame_h);
    static final float time_per_frame = 1f / 20f; // ~1.xx seconds, 16 = fps

    public static class BitmapExplosionParticle
    {
        Rect frame;
        Rect pos;
        float frame_time;

        float dir_x;
        float dir_y;
        float speed;

        public BitmapExplosionParticle()
        {
            frame = new Rect(size_rect);
            pos = new Rect(size_rect);

            frame_time = 0;
            dir_x = 0;
            dir_y = 0;
            speed = 0;
        }
    }

    static final int MAX_PARTICLES = 5;
    static BitmapExplosionParticle[] particles = null;
    static int particle_count = 0;

    public static void init()
    {
        if ( particles == null )
        {
            particle_count = 0;
            particles = new BitmapExplosionParticle[MAX_PARTICLES];

            for (int i = MAX_PARTICLES; i != 0; /* empty */)
            {
                particles[--i] = new BitmapExplosionParticle();
            }
        }
    }

    public static void logic(final float time)
    {
        for (int i = 0, e = particle_count; i < e; /* emtpy */)
        {
            final BitmapExplosionParticle p = particles[i];

            final float fspeed = p.speed * time;
            p.pos.offset(Math.round(p.dir_x * fspeed), Math.round(p.dir_y * fspeed));

            p.frame_time += time;
            if (p.frame_time >= time_per_frame)
            {
                final Rect r = p.frame;
                r.offset(frame_w, 0);
                if (r.left >= image_w)
                {
                    r.offset(-image_w, frame_h);
                    if (r.top >= image_h)
                    {
                        particle_count -= 1;
                        e -= 1;
                        if (i < e)
                        {
                            particles[i] = particles[e];
                            particles[e] = p;
                        }
                        continue;
                    }
                }
                p.frame_time -= time_per_frame;
            }

            i++;
        }
    }

    public static void draw(Canvas canvas)
    {
        final Bitmap image = Res.explosion;
        for (int i = particle_count; i != 0; /* empty */)
        {
            final BitmapExplosionParticle p = particles[--i];
            canvas.drawBitmap(image, p.frame, p.pos, null);
        }

    }

    public static void add(int x, int y, float dir_x, float dir_y, float speed)
    {
        if (particle_count < MAX_PARTICLES)
        {
            final BitmapExplosionParticle p = particles[particle_count++];
            p.frame_time = 0;
            p.dir_x = dir_x;
            p.dir_y = dir_y;
            p.speed = speed;
            p.frame.offsetTo(0, 0);
            p.pos.offsetTo(x - frame_w / 2, y - frame_h / 2);
            Log.i("Explosion", String.format("dir_x=%f dir_y=%f speed=%f", dir_x, dir_y, speed));
        }
    }

}
