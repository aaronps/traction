package dev.aaronps.traction;

import java.util.Random;

import dev.aaronps.traction.SparkParticleSystem.SparkParticle;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ThrustParticleSystem implements ParticleSystem
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
	
	static final float MAX_ALIVE = 0.3f;
	static final float MIN_ALIVE = 0.1f;
	static final float RND_ALIVE = MAX_ALIVE - MIN_ALIVE;
	
	static final float MAX_SPEED= 100f;
	static final float MIN_SPEED = 80f;
	static final float RND_SPEED = MAX_SPEED - MIN_SPEED;
	
	private static final float PARTICLES_PER_THRUST_PER_SECOND = 80;
	
	static final int MAX_PARTICLES = 100;
	static ThrustParticle[] particles = null;
	static int particle_count = 0;
	static Paint thrustPaint = null;
	static Random rnd = new Random();
	
	static boolean active = false;
	static float ship_x = 0;
	static float ship_y = 0;
	
	private static final double anglebase = Math.PI/2;
	private static final double outangle = Math.PI/8 - Math.PI/16;
	
	public ThrustParticleSystem()
	{
		particles = new ThrustParticle[MAX_PARTICLES];
		for ( int i = MAX_PARTICLES; i != 0; /* empty */ )
		{
			particles[--i] = new ThrustParticle();
		}
		
		thrustPaint = new Paint();
		thrustPaint.setColor(0xff00bbff); //blue
//		thrustPaint.setColor(0xffffde00); //yellow center
	}

	@Override
	public void logic(float time)
	{
		if ( active )
		{
			final float left_x = ship_x - 4;
			final float left_y = ship_y + 14;
			final float right_x = ship_x + 4;
			final float right_y = ship_y + 14;
			
			final int to_generate = Math.round(PARTICLES_PER_THRUST_PER_SECOND * time);
			
			for ( int n = 0; n < to_generate; n++ )
			{
				if ( particle_count < MAX_PARTICLES )
				{
					final ThrustParticle p = particles[particle_count++];
					
					final double angle = anglebase + rnd.nextDouble() * outangle;
					
					p.dir_x = (float)Math.cos(angle);
					p.dir_y = (float)Math.sin(angle);
					
					p.x = left_x + ( rnd.nextFloat() * 4) - 2;
					p.y = left_y;
					p.alivetime = rnd.nextFloat() * RND_ALIVE + MIN_ALIVE;
					p.total_alivetime = p.alivetime;
					p.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//					p.dir_y = 1;
				}
				else
				{
					break;
				}
				
				if ( particle_count < MAX_PARTICLES )
				{
					final ThrustParticle p = particles[particle_count++];
					
					final double angle = anglebase + rnd.nextDouble() * outangle;
					
					p.dir_x = (float)Math.cos(angle);
					p.dir_y = (float)Math.sin(angle);
					
					p.x = right_x + ( rnd.nextFloat() * 4) - 2;
					p.y = right_y;
					p.alivetime = rnd.nextFloat() * RND_ALIVE + MIN_ALIVE;
					p.total_alivetime = p.alivetime;
					p.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//					p.dir_y = 1;
				}
				else
				{
					break;
				}
			}
		}
		
		for ( int i = 0, e = particle_count; i < e; /* emtpy */ )
		{
			final ThrustParticle p = particles[i];
			
			final float fspeed = p.speed * time;
			p.x += p.dir_x * fspeed;
			p.y += p.dir_y * fspeed;
			
			p.alivetime -= time;
			if ( p.alivetime <= 0 )
			{
				particle_count -= 1;
				e -= 1;
				if ( i < e )
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
		for ( int i = particle_count; i != 0; /* emtpy */ )
		{
			final ThrustParticle p = particles[--i];
			thrustPaint.setAlpha(Math.round(p.alivetime * 255 / p.total_alivetime));
			final float rad = p.alivetime * 4 / p.total_alivetime;
			canvas.drawCircle(p.x, p.y, rad, thrustPaint);
//			canvas.drawPoint(p.x, p.y, thrustPaint);
		}
	}

	public void setShipLocation(float x, float y)
	{
		ship_x = x;
		ship_y = y;
	}

}
