package dev.aaronps.traction.gamelayers;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import dev.aaronps.traction.Res;

public class ThrustParticleSystem
{
    public static class ThrustParticle
    {
        float x, y;
        float dir_x, dir_y;
        float speed;
        float total_alivetime;
        float alivetime;

        public ThrustParticle()
        {
            x = y = dir_x = dir_y = speed = alivetime = 0;
            total_alivetime = 0;
        }
    }

    static final float MAX_ALIVE = 0.2f; //0.2
    static final float MIN_ALIVE = 0.1f; //0.1
    static final float RND_ALIVE = MAX_ALIVE
            - MIN_ALIVE;

    static final float MAX_SPEED = 100f; // 100
    static final float MIN_SPEED = 80f;  // 80
    static final float RND_SPEED = MAX_SPEED
            - MIN_SPEED;

    private static final float PARTICLES_PER_THRUST_PER_SECOND = 80;

    static final int MAX_PARTICLES = 100;
    static ThrustParticle[] particles = null;
    static int particle_count = 0;
    static Paint thrustPaint = null;

    public static boolean active = false;
    static float ship_x = 0;
    static float ship_y = 0;

    private static final double anglebase = Math.PI / 2;
    private static final double outangle = Math.PI / 8 - Math.PI / 16;

    private static final int MAX_DOUBLES = 128;
    private static final int DOUBLE_MASK = 0x7f;
    private static double[] rnd_double;
    private static int cur_double = 0;

    private static final int MAX_FLOATS = 256;
    private static final int FLOAT_MASK = 0xff;
    private static float[] rnd_float;
    private static int cur_float = 0;

    private static RectF drect = new RectF();
     
    // @SuppressLint( "NewApi")
    public static void init()
    {
        if ( particles == null )
        {
            particles = new ThrustParticle[MAX_PARTICLES];
            for (int i = MAX_PARTICLES; i != 0; /* empty */)
            {
                particles[--i] = new ThrustParticle();
            }

            rnd_double = new double[MAX_DOUBLES];
            rnd_float = new float[MAX_FLOATS];

            Random rnd = new Random();
            for (int n = 0; n < MAX_DOUBLES; n++)
            {
                rnd_double[n] = rnd.nextDouble();
            }
            for (int n = 0; n < MAX_FLOATS; n++)
            {
                rnd_float[n] = rnd.nextFloat();
            }
            rnd = null;

            thrustPaint = new Paint();
            // thrustPaint.setColor(0xff00bbff); //blue
            thrustPaint.setColor(0xffffde00); // yellow center
            // this is the effect I wanted
            // thrustPaint.setXfermode( new PorterDuffXfermode( Mode.ADD) );

            // thrustPaint.setColor(0xfff2ef12);
        }
    }

    public static void logic(float time)
    {
        if (active)
        {
            final float left_x = ship_x - 4;
            final float left_y = ship_y + 14;
            final float right_x = ship_x + 4;
            final float right_y = ship_y + 14;

            final int to_generate = Math.round(PARTICLES_PER_THRUST_PER_SECOND * time);

            for (int n = 0; n < to_generate; n++)
            {
                if (particle_count < MAX_PARTICLES)
                {
                    final ThrustParticle p = particles[particle_count++];

                    final double angle = anglebase + rnd_double[cur_double++] * outangle;
                    cur_double &= DOUBLE_MASK;

                    p.dir_x = (float) Math.cos(angle);
                    p.dir_y = (float) Math.sin(angle);

                    p.x = left_x + (rnd_float[cur_float++] * 4) - 2;
                    cur_float &= FLOAT_MASK;
                    p.y = left_y;

                    p.alivetime = rnd_float[cur_float++] * RND_ALIVE + MIN_ALIVE;
                    cur_float &= FLOAT_MASK;
                    p.total_alivetime = p.alivetime;

                    p.speed = rnd_float[cur_float++] * RND_SPEED + MIN_SPEED;
                    cur_float &= FLOAT_MASK;
                    // p.dir_y = 1;
                }
                else
                {
                    break;
                }

                if (particle_count < MAX_PARTICLES)
                {
                    final ThrustParticle p = particles[particle_count++];

                    final double angle = anglebase + rnd_double[cur_double] * outangle;
                    cur_double &= DOUBLE_MASK;

                    p.dir_x = (float) Math.cos(angle);
                    p.dir_y = (float) Math.sin(angle);

                    p.x = right_x + (rnd_float[cur_float++] * 4) - 2;
                    cur_float &= FLOAT_MASK;
                    p.y = right_y;

                    p.alivetime = rnd_float[cur_float++] * RND_ALIVE + MIN_ALIVE;
                    cur_float &= FLOAT_MASK;
                    p.total_alivetime = p.alivetime;

                    p.speed = rnd_float[cur_float++] * RND_SPEED + MIN_SPEED;
                    cur_float &= FLOAT_MASK;
                    // p.dir_y = 1;
                }
                else
                {
                    break;
                }
            }
        }

        for (int i = 0, e = particle_count; i < e; /* emtpy */)
        {
            final ThrustParticle p = particles[i];

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

    public static void draw(Canvas canvas)
    {
         final RectF r = drect;
        for (int i = particle_count; i != 0; /* emtpy */)
        {
            final ThrustParticle p = particles[--i];
            thrustPaint.setAlpha(Math.round(p.alivetime * 255 / p.total_alivetime));

             final float siz = p.alivetime * 8 / p.total_alivetime; // will
//             use double of this size
             r.set( p.x-siz, p.y-siz, p.x+siz, p.y+siz);
             canvas.drawBitmap( Res.yepart, null, r, thrustPaint );
//             canvas.drawBitmap( Res.yepart, p.x-8, p.y-8, thrustPaint );
            
//            final float rad = p.alivetime * 4 / p.total_alivetime;
//            canvas.drawCircle(p.x, p.y, rad, thrustPaint);

//             canvas.drawPoint(p.x, p.y, thrustPaint);
        }
    }

    public static void setShipLocation(float x, float y)
    {
        ship_x = x;
        ship_y = y;
    }

}
