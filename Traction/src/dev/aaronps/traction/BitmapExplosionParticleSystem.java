package dev.aaronps.traction;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class BitmapExplosionParticleSystem implements ParticleSystem
{
	// if image changes, update here
	static final int frame_w = 64;
	static final int frame_h = 64;
	static final int image_w = 512;
	static final int image_h = 256;
	static final Rect size_rect = new Rect(0,0,frame_w,frame_h);
	static final long frame_ms = 1000/16; // ~1.xx seconds, 16 = fps
	
	public static class BitmapExplosionParticle
	{
		Rect frame;
		Rect pos;
		long f_time;
		
		float dir_x;
		float dir_y;
		float speed;
		
		public BitmapExplosionParticle()
		{
			frame = new Rect(size_rect);
			pos = new Rect(size_rect);
			
			f_time = 0;
			dir_x = 0;
			dir_y = 0;
			speed = 0;
		}
	}

	static int MAX_PARTICLES = 5;
	static BitmapExplosionParticle[] active_particles = null;
	static BitmapExplosionParticle[] inactive_particles = null;
	static int active_particles_count = 0;
	static int inactive_particles_count = 0;
	
	public BitmapExplosionParticleSystem()
	{
		active_particles = new BitmapExplosionParticle[MAX_PARTICLES];
		inactive_particles = new BitmapExplosionParticle[MAX_PARTICLES];
		
		for ( int i = MAX_PARTICLES; i != 0; /* empty */ )
		{
			inactive_particles[--i] = new BitmapExplosionParticle();
		}
		
		inactive_particles_count = MAX_PARTICLES;
		
	}
	
	@Override
	public void logic(final long time)
	{
		for ( int i = 0, e = active_particles_count; i < e; /* emtpy */ )
		{
			final BitmapExplosionParticle p = active_particles[i];
			
			final float fspeed = p.speed * time;
			p.pos.offset(Math.round(p.dir_x*fspeed), Math.round(p.dir_y * fspeed));
			
			p.f_time += time;
			if ( p.f_time >= frame_ms )
			{
				final Rect r = p.frame;
				r.offset(frame_w, 0);
				if ( r.left >= image_w )
				{
					r.offset(-image_w, frame_h);
					if ( r.top >= image_h )
					{
						inactive_particles[inactive_particles_count++] = p;
						active_particles_count -= 1;
						e -= 1;
						if ( i < e )
						{
							active_particles[i] = active_particles[e];
						}
						continue;
					}
				}
				p.f_time %= frame_ms;
			}
			
			i++;
		}
	}

	@Override
	public void draw(Canvas canvas)
	{
		final Bitmap image = GameResources.explosion;
		for ( int i = active_particles_count; i != 0; /* empty */ )
		{
			final BitmapExplosionParticle p = active_particles[--i];
			canvas.drawBitmap(image, p.frame, p.pos, null);
		}
		
	}

	public void add(int x, int y, float dir_x, float dir_y)
	{
		if ( inactive_particles_count > 0 )
		{
			final BitmapExplosionParticle p = inactive_particles[--inactive_particles_count];
			p.f_time = 0;
			p.dir_x = dir_x;
			p.dir_y = dir_y;
			p.frame.offsetTo(0, 0);
			p.pos.offsetTo(x - frame_w/2, y - frame_h/2);
			active_particles[active_particles_count++] = p;
		}
	}

}
