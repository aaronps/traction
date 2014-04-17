package dev.aaronps.traction;

import android.graphics.Bitmap;

public class States
{
	State prev;
	State current;
	DrawState draw_state;
	
	public States(final int debril_count)
	{
		prev    = new State(debril_count);
		current = new State(debril_count);
		draw_state = new DrawState(debril_count+2, 5);
	}
	
	final void copyCurrentToPrev()
	{
		for(int n = 0, e = current.debrils.length; n < e; n++ )
		{
			final Debril src = current.debrils[n];
			final Debril dst = prev.debrils[n];
			
			dst.x = src.x;
			dst.y = src.y;
			dst.dir_x = src.dir_x;
			dst.dir_y = src.dir_y;
			dst.speed = src.speed;
			
			dst.max_speed = src.max_speed;
			dst.min_speed = src.min_speed;
			dst.acceleration = src.acceleration;
		}
		
		{
			final Ship dst = prev.ship;
			final Ship src = current.ship;
			
			dst.x = src.x;
			dst.y = src.y;
			dst.dir_x = src.dir_x;
			dst.dir_y = src.dir_y;
			dst.speed = src.speed;
			dst.shield = src.shield;
			dst.shield_active = src.shield_active;
			dst.shield_counter = src.shield_counter;
		}

		
	}
	
	final void swapStates()
	{
		final State s = prev;
		prev = current;
		current = s;
	}
	
	final void interpolShip(final float c_rate)
	{
		final float p_rate = 1.0f - c_rate;
		final float x = (current.ship.x * c_rate + prev.ship.x * p_rate);
		final float y = (current.ship.y * c_rate + prev.ship.y * p_rate);

		draw_state.add( GameResources.ship,
						x - GameResources.ship_offset_x,
						y - GameResources.ship_offset_y);
		
		if ( current.ship.shield )
		{
			draw_state.add( current.ship.shield_active ? GameResources.ship_aura_active : GameResources.ship_aura,
							x - GameResources.ship_aura_offset_x,
							y - GameResources.ship_aura_offset_y);
			
		}
	}
	

	final void interpolDebrils(final float c_rate)
	{
		final float p_rate = 1.0f - c_rate;
		
		final Bitmap i = GameResources.debril;
		final Debril[] cur_d = current.debrils;
		final Debril[] pre_d = prev.debrils;
		
		for ( int n = 0, e = cur_d.length; n < e; n++ )
		{
			draw_state.add( i, 
							(cur_d[n].x * c_rate + pre_d[n].x * p_rate)-GameResources.debril_offset_x,
							(cur_d[n].y * c_rate + pre_d[n].y * p_rate)-GameResources.debril_offset_y);
		}
	}
	
	final void interpolParticles(final long frame_time)
	{
		final float ftime = frame_time/1000f;
		draw_state.explosions.logic(ftime);
		draw_state.sparks.logic(ftime);
		
	}

	final void resetShip()
	{
		Ship s = current.ship;
		s.x = s.y = s.dir_x = s.dir_y = s.speed = 0;
		s.shield = s.shield_active = false;
		
		s = prev.ship;
		s.x = s.y = s.dir_x = s.dir_y = s.speed = 0;
		s.shield = s.shield_active = false;
	}

}
