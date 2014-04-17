package dev.aaronps.traction;

import android.graphics.Bitmap;

public class DrawState
{
	public Sprite[] sprites;
	public int sprite_count;

	BitmapExplosionParticleSystem explosions;
	SparkParticleSystem sparks;
	
	public Sprite[] topLayer;
	public int topLayerCount;
	
	public long alive_time;
	public long last_fps;
	
	public DrawState(final int max_sprites, final int max_particles)
	{
		sprite_count = 0;
		sprites = new Sprite[max_sprites];
		for ( int n = 0; n < max_sprites; n++ )
		{
			sprites[n] = new Sprite();
		}
		
		explosions = new BitmapExplosionParticleSystem();
		sparks = new SparkParticleSystem();
		
		topLayerCount = 0;
		topLayer = new Sprite[5];
		for ( int n = 0; n < 5; n++ )
		{
			topLayer[n] = new Sprite();
		}
		
		alive_time = 0;
		last_fps = 0;
	}
	
	public void reset() { sprite_count = 0; topLayerCount = 0; }
	
	public void add(final Bitmap image, final float x, final float y)
	{
		final Sprite s = sprites[sprite_count++];
		s.image = image;
		s.x = x;
		s.y = y;
	}
	
	public void addTop(final Bitmap image, final float x, final float y)
	{
		final Sprite s = topLayer[topLayerCount++];
		s.image = image;
		s.x = x;
		s.y = y;
	}
	
	public void addExplosion(final float x, final float y, final float dir_x, final float dir_y, final float speed)
	{
		explosions.add(Math.round(x), Math.round(y), dir_x, dir_y, speed);
//		ExplosionParticle p = new ExplosionParticle(Math.round(x), Math.round(y), dir_x, dir_y, ms_speed);
//		particles[particle_count++] = p;
	}
	
	public void addSpark(final float x, final float y, final float dir_x, final float dir_y, final float speed)
	{
		sparks.addSpark(x, y, dir_x, dir_y);
	}

}
