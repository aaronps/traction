package dev.aaronps.traction.gamelayers;

import java.util.Random;

import dev.aaronps.traction.Res;
import dev.aaronps.traction.ParticleSystem;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class SparkParticleSystem implements ParticleSystem, GameLayer
{
    public static class SparkParticle
    {
        float x, y;
        float dir_x, dir_y;
        float speed;
        float total_alivetime;
        float alivetime;

        public SparkParticle()
        {
            x = y = dir_x = dir_y = speed = alivetime = 0;
            total_alivetime = 0;
        }
    }

    static final double HALFPI = Math.PI / 2;

    static final float MAX_ALIVE = 0.4f;
    static final float MIN_ALIVE = 0.1f;
    static final float RND_ALIVE = MAX_ALIVE - MIN_ALIVE;

    static final float MAX_SPEED = 100f;
    static final float MIN_SPEED = 40f;
    static final float RND_SPEED = MAX_SPEED - MIN_SPEED;

    static final int MAX_PARTICLES = 100;
    static SparkParticle[] particles = null;
    static int particle_count = 0;
    static Paint sparkPaint = null;
    static Random rnd = new Random();

    private static final RectF drect = new RectF();

    public SparkParticleSystem()
    {
        particles = new SparkParticle[MAX_PARTICLES];
        for (int i = MAX_PARTICLES; i != 0; /* empty */)
        {
            particles[--i] = new SparkParticle();
        }

        sparkPaint = new Paint();
//		sparkPaint.setColor(0xff00bbff); //blue
//		sparkPaint.setColor(0xffffde00); //yellow center
    }

    @Override
    public void logic(final float time)
    {
        for (int i = 0, e = particle_count; i < e; /* emtpy */)
        {
            final SparkParticle p = particles[i];

            final float fspeed = p.speed * time;
            p.x += p.dir_x * fspeed;
            p.y += p.dir_y * fspeed;

            p.alivetime -= time;
            if (p.alivetime <= 0)
            {
                particle_count -= 1;
                e -= 1;
                if (i < e)
                {
                    particles[i] = particles[e];
                    particles[e] = p;
                }
            }

            i++;
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        final RectF r = drect;
        for (int i = particle_count; i != 0; /* emtpy */)
        {
            final SparkParticle p = particles[--i];
            sparkPaint.setAlpha(Math.round(p.alivetime * 255 / p.total_alivetime));

            final float siz = p.alivetime * 4 / p.total_alivetime; // will use double of this size
            r.set(p.x - siz, p.y - siz, p.x + siz, p.y + siz);
            canvas.drawBitmap(Res.yepart, null, r, sparkPaint);
//          canvas.drawBitmap( GameResources.yepart, p.x-8, p.y-8, thrustPaint );

//			final float rad = p.alivetime * 2f / p.total_alivetime;
//			canvas.drawCircle(p.x, p.y, rad, sparkPaint);
//			canvas.drawPoint(p.x, p.y, sparkPaint);
        }
    }

    public void addSpark(final float x, final float y, final float dir_x, final float dir_y)
    {
        final double outangle = Math.atan2(dir_y, dir_x) - HALFPI;
        for (int n = 10; n != 0; n--)
        {
            if (particle_count < MAX_PARTICLES)
            {
                final SparkParticle p = particles[particle_count++];
                final double angle = outangle + rnd.nextDouble() * Math.PI;

                p.dir_x = (float) Math.cos(angle);
                p.dir_y = (float) Math.sin(angle);
                p.x = x;
                p.y = y;

                p.alivetime = (rnd.nextFloat() * RND_ALIVE) + MIN_ALIVE;
                p.total_alivetime = p.alivetime;
                p.speed = (rnd.nextFloat() * RND_SPEED) + MIN_SPEED;
            }
            else
            {
                break;
            }
        }
    }

}
