package dev.aaronps.traction.gamelayers;

import android.graphics.Bitmap;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.Res;

public class BackgroundStarsParticleSystem
{
    public static class StarParticle
    {
        RectF pos;
        float speed;
        public StarParticle()
        {
            pos = new RectF();
            speed = 0f;
        }
    }
    
    static final float MAX_SPEED = 160f; //30
    static final float MIN_SPEED = 4f; //4
    static final float RND_SPEED = MAX_SPEED - MIN_SPEED;

    static final int MAX_PARTICLES = 50;
//    static float[] ppoints = null;
//    static float[] pspeeds = null;
    static int particle_count = 0;
    static Paint[] starPaint = null;
    static Random rnd = new Random();

    private static final float time_accel_rate = 0.5f;
    private static final float slowmo_time_rate = 0.02f;

    public static boolean slowmo = false;
    public static float time_rate = slowmo_time_rate;
    
    static StarParticle particles[] = null;

    public static void init()
    {
        if ( particles == null )
        {
            particles = new StarParticle[MAX_PARTICLES];
            for (int i = MAX_PARTICLES; i != 0; /* empty */)
            {
                StarParticle star = new StarParticle();
                final float ssize = (rnd.nextFloat() * 12f)+4f;
         
                star.pos.set(0, 0, ssize, ssize);
                star.pos.offset((rnd.nextFloat() * Config.WORLD_SIZE_X) - Config.WORLD_SIZE_X / 2,
                                (rnd.nextFloat() * Config.WORLD_SIZE_Y) - Config.WORLD_SIZE_Y / 2);
                
                star.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
                
                particles[--i] = star;
                particle_count += 1;
            }
        }
        
//        particle_count = MAX_PARTICLES;
        
//        if ( ppoints == null )
//        {
//            starPaint = new Paint[3];
//            starPaint[0] = new Paint();
//            starPaint[0].setColor(0xffcccccc);
//            starPaint[1] = new Paint();
//            starPaint[1].setColor(0xffffffff);
//            starPaint[2] = new Paint();
//            starPaint[2].setColor(0xffffffff);
//
//            starPaint[0].setStrokeWidth(2.5f);
//            starPaint[0].setStrokeCap(Cap.ROUND);
//
//            ppoints = new float[MAX_PARTICLES * 2];
//            pspeeds = new float[MAX_PARTICLES];
//            for (int i = 0, ixy = 0; i < MAX_PARTICLES; i++)
//            {
//                ppoints[ixy++] = (rnd.nextFloat() * Config.WORLD_SIZE_X) - Config.WORLD_SIZE_X / 2;
//                ppoints[ixy++] = (rnd.nextFloat() * Config.WORLD_SIZE_Y) - Config.WORLD_SIZE_Y / 2;
//                pspeeds[i] = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//            }
//
//            particle_count = MAX_PARTICLES;
//        }
    }
    
    public static void setSlowSpeed()
    {
        time_rate = slowmo_time_rate;
    }
    
    public static void logic(final float time)
    {
        float frate;
        if (slowmo)
        {
            if (time_rate > slowmo_time_rate)
            {
                time_rate -= time * time_accel_rate * 2;
                if (time_rate < slowmo_time_rate)
                {
                    time_rate = slowmo_time_rate;
                }
            }
            frate = time_rate;
        }
        else
        {
            if (time_rate < 1.0f)
            {
                time_rate += time * time_accel_rate;
                if (time_rate > 1.0f)
                {
                    time_rate = 1.0f;
                }
            }
            frate = time_rate;
        }

        final float ftime = time * frate;
//        for (int i = 0, iy = 1; i < MAX_PARTICLES; iy += 2, i++)
//        {
//            final float fspeed = pspeeds[i] * ftime;
//            ppoints[iy] += fspeed;
//            if (ppoints[iy] >= Config.WORLD_MAX_Y)
//            {
//                ppoints[iy] = Config.WORLD_MIN_Y - 10;
//                ppoints[iy - 1] = (rnd.nextFloat() * Config.WORLD_SIZE_X) - Config.WORLD_SIZE_X / 2;
//                pspeeds[i] = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//            }
//        }
        
        for (int i = 0; i < MAX_PARTICLES; i++)
        {
            final StarParticle p = particles[i];
            final float fspeed = p.speed * ftime;
            
            p.pos.offset(0, fspeed);
            
            if ( p.pos.top >= Config.WORLD_MAX_Y )
            {
                p.pos.offsetTo((rnd.nextFloat() * Config.WORLD_SIZE_X) - Config.WORLD_SIZE_X / 2, Config.WORLD_MIN_Y - 10);
                p.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
            }
        }
    }

    public static void draw(Canvas canvas)
    {
        final Bitmap pic = Res.star_bitmap;
        final Rect sr = Res.star_rect;
        for (int i = particle_count; i != 0; /* emtpy */)
        {
            final StarParticle dst = particles[--i];
            canvas.drawBitmap(pic, null, dst.pos, null);
        }
        
//        canvas.drawBitma
//        canvas.drawPoints(ppoints, starPaint[0]);
    }

}
