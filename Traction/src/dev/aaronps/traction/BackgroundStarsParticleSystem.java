package dev.aaronps.traction;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;

public class BackgroundStarsParticleSystem implements ParticleSystem
{
	static final float min_x = -350f;
	static final float min_y = -450f;

	static final float max_x = 350f;
	static final float max_y = 450f;
	
	static final float x_size = max_x - min_x;
	static final float y_size = max_y - min_y;
	
	static final float MAX_SPEED= 160f; //30
	static final float MIN_SPEED = 4f; //4
	static final float RND_SPEED = MAX_SPEED - MIN_SPEED;
	
	static final int MAX_PARTICLES = 100;
	static float[] ppoints = null;
	static float[] pspeeds = null;
	static int particle_count = 0;
	static Paint[] starPaint = null;
	static Random rnd = new Random();

	private static final float time_accel_rate = 0.5f;
	private static final float slowmo_time_rate = 0.02f;
	static boolean slowmo = false;
	static float time_rate = slowmo_time_rate;
	
	
	public BackgroundStarsParticleSystem()
	{
		starPaint = new Paint[3];
		starPaint[0] = new Paint(); starPaint[0].setColor(0xffcccccc);
		starPaint[1] = new Paint(); starPaint[1].setColor(0xffffffff);
		starPaint[2] = new Paint(); starPaint[2].setColor(0xffffffff);
		
		starPaint[0].setStrokeWidth(2.5f);
		starPaint[0].setStrokeCap(Cap.ROUND);
		
		
		ppoints = new float[MAX_PARTICLES*2];
		pspeeds= new float[MAX_PARTICLES];
		for ( int i = 0, ixy = 0; i < MAX_PARTICLES; i++ )
		{
			ppoints[ixy++] = (rnd.nextFloat() * x_size) - x_size / 2;  
			ppoints[ixy++] = (rnd.nextFloat() * y_size) - y_size / 2;
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
		for ( int i = 0, iy = 1; i < MAX_PARTICLES; iy+=2, i++ )
		{
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
	}

}
