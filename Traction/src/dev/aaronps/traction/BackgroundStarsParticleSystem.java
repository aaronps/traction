package dev.aaronps.traction;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;

public class BackgroundStarsParticleSystem implements ParticleSystem
{
//	public static class BackgroundStarParticle
//	{
//		float x, y;
//		float speed;
//		Paint paint;
//		
//		public BackgroundStarParticle()
//		{
//			x = y = speed = 0;
//			paint = null;
//		}
//	}
	
	static final float min_x = -350f;
	static final float min_y = -350f;

	static final float max_x = 350f;
	static final float max_y = 350f;
	
	static final float x_size = max_x - min_x;
	
	static final float MAX_SPEED= 80f; //30
	static final float MIN_SPEED = 4f; //4
	static final float RND_SPEED = MAX_SPEED - MIN_SPEED;
	
	static final int MAX_PARTICLES = 100;
//	static BackgroundStarParticle[] particles = null;
	static float[] ppoints = null;
	static float[] pspeeds = null;
	static int particle_count = 0;
	static Paint[] starPaint = null;
	static Random rnd = new Random();

	private static final float time_accel_rate = 0.5f;
	private static final float slowmo_time_rate = 0.2f;
	static boolean slowmo = false;
	static float time_rate = slowmo_time_rate;
	
	
	public BackgroundStarsParticleSystem()
	{
		starPaint = new Paint[3];
		starPaint[0] = new Paint(); starPaint[0].setColor(0xffaaaaaa);
		starPaint[1] = new Paint(); starPaint[1].setColor(0xffffffff);
		starPaint[2] = new Paint(); starPaint[2].setColor(0xffffffff);
		
		
		ppoints = new float[MAX_PARTICLES*2];
		pspeeds= new float[MAX_PARTICLES];
//		particles = new BackgroundStarParticle[MAX_PARTICLES];
//		for ( int i = MAX_PARTICLES; i != 0; /* empty */ )
		for ( int i = 0, ixy = 0; i < MAX_PARTICLES; i++ )
		{
//			final BackgroundStarParticle p = new BackgroundStarParticle();
			
//			p.x = (rnd.nextFloat() * x_size) - x_size / 2;  
//			p.y = (rnd.nextFloat() * x_size) - x_size / 2;
//			p.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//			p.paint = starPaint[0];
//			
//			particles[--i] = p;
			ppoints[ixy++] = (rnd.nextFloat() * x_size) - x_size / 2;  
			ppoints[ixy++] = (rnd.nextFloat() * x_size) - x_size / 2;
			pspeeds[i] = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
		}
		
		particle_count = MAX_PARTICLES;
	}
	

	@Override
	public void logic(final float time)
	{
		float frate;
		if ( slowmo )
		{
			if ( time_rate > slowmo_time_rate )
			{
				time_rate -= time * time_accel_rate * 2;
				if ( time_rate < slowmo_time_rate ) time_rate = slowmo_time_rate;
			}
			frate = time_rate;
		}
		else
		{
			if ( time_rate < 1.0f )
			{
				time_rate += time * time_accel_rate;
				if ( time_rate > 1.0f ) { time_rate = 1.0f; }
			}
			frate = time_rate;
		}
		
		final float ftime = time * frate;
//		for ( int i = 0, e = particle_count; i < e; /* emtpy */ )
		for ( int i = 0, iy = 1; i < MAX_PARTICLES; iy+=2, i++ )
		{
//			final BackgroundStarParticle p = particles[i];
			
//			final float fspeed = p.speed * ftime;
//			p.y += fspeed;
//			if ( p.y >= max_y )
//			{
//				p.y = min_y - 10;
//				p.x = (rnd.nextFloat() * x_size) - x_size / 2;
//				p.speed = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
//			}
//			
//			i++;
			final float fspeed = pspeeds[i] * ftime;
			ppoints[iy] += fspeed;
			if ( ppoints[iy] >= max_y )
			{
				ppoints[iy] = min_y - 10;
				ppoints[iy-1] = (rnd.nextFloat() * x_size) - x_size / 2;
				pspeeds[i] = rnd.nextFloat() * RND_SPEED + MIN_SPEED;
			}
		}
	}

	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawPoints(ppoints, starPaint[0]);
//		for ( int i = particle_count; i != 0; /* emtpy */ )
//		{
//			final BackgroundStarParticle p = particles[--i];
//			canvas.drawPoint(p.x, p.y, p.paint);
//		}
	}

}
