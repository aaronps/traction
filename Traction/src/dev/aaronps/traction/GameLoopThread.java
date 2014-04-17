package dev.aaronps.traction;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class GameLoopThread extends Thread
{
	/*
	 * Notes about logic fps:
	 * 
	 * Each second, times per second which are rounded numbers:
	 * 1000/50 = 20ms between
	 * 1000/40 = 25ms between
	 * 1000/25 = 40ms between
	 * 1000/20 = 50ms between
	 * 1000/10 = 100ms between
	 * 1000/8  = 125ms between
	 * 
	 * rest are to small to take into account
	 * 
	 * Notes about sizes (from stackoverflow):
xhdpi = 100% image
hdpi = 75% image of xhdpi image
mdpi = 50% image of xhdpi image
ldpi = 50% image of hdpi image
i.e :

if you have 96 x 96 image in xhdpi then, you need to put

72 x 72 in hdpi folder - ( 75 % of xhdpi )
48 x 48 in mdpi folder - ( 50 % of xhdpi )
36 x 36 in ldpi folder - ( 50 % of hdpi )
	 * 
	 * 
	 * 
	 */
	
	static final long LPS = 25;
	static final long DELAY_BETWEEN_LOGICS = 1000/LPS;
	static final float ACCEL_PER_SECOND = 25.0f;
	static final float ACCEL_PER_LOGIC = ACCEL_PER_SECOND / LPS;
	static final float MAX_SPEED = 240f;
	static final float MAX_SPEED_LOGIC = MAX_SPEED / LPS;
	static final float REDUCED_SPEED = 15f;
	static final float REDUCED_SPEED_LOGIC = REDUCED_SPEED / LPS;
	
	static final float min_x = -350f;
	static final float min_y = -350f;

	static final float max_x = 350f;
	static final float max_y = 350f;
	
	static final RectF SMOVE_AREA = new RectF(min_x + 50, min_y + 50, max_x - 50, max_y - 50);
	
	private static enum GameState
	{
		Init, EnterStart, ReadyToStart, Game, EnterDeath, ReducingDeath, Death;
	}
	
	private static Rect shipRect = new Rect();
	private static Rect debrilRect = new Rect();
	private static Rect intersectRect = new Rect();
	
	private GameView view;
    private boolean running = false;
    private States states;
    private int num_points = 30;
    
    private boolean pressed = false;
    
    public GameLoopThread(GameView view)
    {
//    	System.out.println("ACCEL_PER_LOGIC = " + ACCEL_PER_LOGIC);
    	this.view = view;
    	
    	states = new States(num_points);
    }

    public void moveShip(final float dx, final float dy)
    {
    	// I set 'current' here because later it will be swapped, and these values readed from 'prev'
    	// so by setting them in current. it fucking works.
    	states.current.ship.dir_x -= dx;
    	states.current.ship.dir_y -= dy;
    	states.current.ship.speed = 1f;
//    	states.ship_x -= dx;
//    	states.ship_y -= dy;
    }
    
    public void setRunning(boolean run)
    {
    	running = run;
    }
    
    private static final void shipMoveLogic(final Ship pship, final Ship ship)
    {
    	ship.x = pship.x + pship.dir_x * pship.speed;
		ship.y = pship.y + pship.dir_y * pship.speed;
		
		if ( ship.x < SMOVE_AREA.left ) { ship.x = SMOVE_AREA.left; }
		else if ( ship.x > SMOVE_AREA.right) { ship.x = SMOVE_AREA.right; }
		
		if ( ship.y < SMOVE_AREA.top) { ship.y = SMOVE_AREA.top; }
		else if ( ship.y > SMOVE_AREA.bottom) { ship.y = SMOVE_AREA.bottom; }
		
		ship.dir_x = ship.dir_y = ship.speed = 0;
    }
    
    private static final void debrilMoveLogic(final Debril[] prev_array, final Debril[] cur_array)
    {
    	final int e = prev_array.length;
    	for ( int n = 0; n < e; n++ )
		{
			final Debril prev = prev_array[n];
			final Debril cur = cur_array[n];
			
//			cur.x = prev.x;
//			cur.y = prev.y;
//			cur.dir_x = prev.dir_x;
//			cur.dir_y = prev.dir_y;
//			cur.speed = prev.speed;
//			
//			cur.max_speed = prev.max_speed;
//			cur.min_speed = prev.min_speed;
//			cur.acceleration = prev.acceleration;
//			
			cur.max_speed = prev.max_speed;
			cur.min_speed = prev.min_speed;

			if ( prev.acceleration != 0 )
			{
				cur.speed = prev.speed + prev.acceleration;
				if ( cur.speed >= cur.max_speed && prev.acceleration > 0 )
				{
					cur.speed = cur.max_speed;
					cur.acceleration = 0;
				}
				else if ( cur.speed <= cur.min_speed && prev.acceleration < 0 )
				{
					cur.speed = cur.min_speed;
					cur.acceleration = 0;
				}
				else
				{
					cur.acceleration = prev.acceleration;
				}
			}
			else
			{
				cur.speed = prev.speed;
				cur.acceleration = 0;
			}
			/*
			 * s = ut + 0.5at^2
			 * 
			 * s: distance to travel
			 * t: time
			 * a: acceleration
			 * 
			 */
			
			final float half_accel = prev.acceleration / 2;
			
			cur.x = prev.x + prev.dir_x * prev.speed + prev.dir_x * half_accel;
			
			if ( (cur.x <= min_x && prev.dir_x < 0)
				|| (cur.x >= max_x && prev.dir_x > 0) )
			{
				cur.dir_x = -prev.dir_x;
			}
			else
			{
				cur.dir_x = prev.dir_x;
			}
			
			cur.y = prev.y + prev.dir_y * prev.speed + prev.dir_y * half_accel;
			if ( (cur.y <= min_y && prev.dir_y < 0) 
				|| (cur.y >= max_y && prev.dir_y > 0) )
			{
				cur.dir_y = -prev.dir_y;
			}
			else
			{
				cur.dir_y = prev.dir_y;
			}
		}
    }
    
    private final boolean shipCollisionLogic(final Ship ship, final Debril[] debrils)
    {
    	final Rect ship_rect = shipRect;
        final Rect debril_rect = debrilRect;
        final Rect intersectionRect = intersectRect;
        
		ship_rect.offsetTo((int)ship.x - GameResources.ship_offset_x, (int)ship.y - GameResources.ship_offset_y);
        
		final int e = debrils.length;
		for ( int n = 0; n < e; n++ )
		{
//			final Debril prev = prev.debrils[n];
			final Debril cur = debrils[n];
			
			debril_rect.offsetTo((int)cur.x - GameResources.debril_offset_x, (int)cur.y - GameResources.debril_offset_y);
			
			if ( intersectionRect.setIntersect(ship_rect, debril_rect) )
			{
				if ( GameResources.shipMask.collidesWith(
											intersectionRect.left - ship_rect.left,
											intersectionRect.top - ship_rect.top,
											intersectionRect.height(),
											GameResources.debrilMask,
											intersectionRect.left - debril_rect.left,
											intersectionRect.top - debril_rect.top )
										   )
				{
					return true;
				}
			}
		}
        
    	return false;
    }
    
    private final void shieldCollisionLogic(final Ship ship, final Debril[] debrils, final Debril[] prev_debrils)
    {
    	final float shield_x = ship.x;
    	final float shield_y = ship.y;
    	
    	final float shield_radii = 32;
    	final float debril_radii = 8;
    	final float combined_radii = shield_radii + debril_radii;
    	
    	final float combined_radii_sq = combined_radii * combined_radii;
    	
    	final int e = debrils.length;
		for ( int n = 0; n < e; n++ )
		{
			final Debril debril = debrils[n];
			final float shield_debril_dist_x = shield_x - debril.x;
			final float shield_debril_dist_y = shield_y - debril.y;
			final float shield_debril_dist_sq = shield_debril_dist_x * shield_debril_dist_x + shield_debril_dist_y * shield_debril_dist_y;
			/*
			 * We can do like this because the current parameters for speed
			 * doesn't allow a ball to cross over the shield without intersecting
			 * with it in one of the loop, max is 1000, this is because:
			 * radius of shield = 32
			 * radius of ball = 8
			 * combined radius = 40
			 * logic calculations per second = 25
			 * max speed of balls tested = 300
			 * 
			 * so, each logic the ball will move at most 300/25 = 12 which is
			 * less than the combine radius. for this, 1000/25 = 40 which is 
			 * the combined radius.
			 * 
			 * SO, the move speed must be less than 1000 per second or collision
			 * may fail (it might not and 1000 be the limit, but better safe)
			 * 
			 */
			if ( shield_debril_dist_sq <= combined_radii_sq ) 
			{
				final Debril prev_debril = prev_debrils[n];
				
				final float C_x = shield_x - prev_debril.x;
				final float C_y = shield_y - prev_debril.y;
				final float D_col = prev_debril.dir_x * C_x + prev_debril.dir_y * C_y;
				
				// the ball might be already inside the shield, if so, push it out
				if ( D_col <= 0 )
				{
					float from_shield_to_debril_x = prev_debril.x - shield_x;
	                float from_shield_to_debril_y = prev_debril.y - shield_y;
	                final float fstd = (float)Math.sqrt(from_shield_to_debril_x * from_shield_to_debril_x + from_shield_to_debril_y*from_shield_to_debril_y);
	                from_shield_to_debril_x /= fstd;
	                from_shield_to_debril_y /= fstd;
	                
	                final float nx = shield_x + from_shield_to_debril_x * combined_radii;
	                final float ny = shield_y + from_shield_to_debril_y * combined_radii;

	                debril.x = nx;
	                debril.y = ny;
	                debril.dir_x = from_shield_to_debril_x;
	                debril.dir_y = from_shield_to_debril_y;
	                
	                ship.shield_active = true;
					ship.shield_counter = Math.round(LPS)/2;
	                view.soundPool.play(view.shieldHitSoundId, 0.8f, 0.8f, 0, 0, 1.0f);

					continue;
				}
				
				final float C_len = (float)Math.sqrt(C_x*C_x + C_y*C_y);
				final float F = (C_len*C_len) - (D_col*D_col);
				final float T = combined_radii_sq - F;
				
				// if T < 0 no col
				
				if ( T < 0 )
				{
					System.out.println("T < 0!!!");
				}
				
				final float extra_move = (float)Math.sqrt(T);
				final float Travel_dist = D_col - extra_move;
				
				// point of collision
				debril.x = prev_debril.x + prev_debril.dir_x * Travel_dist;
				debril.y = prev_debril.y + prev_debril.dir_y * Travel_dist;
				
				final float vec_from_shield_x = (debril.x - shield_x) / combined_radii;
	            final float vec_from_shield_y = (debril.y - shield_y) / combined_radii;
	            
	            // add spark?
	            
	            states.draw_state.addSpark( shield_x + vec_from_shield_x * shield_radii,
	            							shield_y + vec_from_shield_y * shield_radii,
	            							vec_from_shield_x, vec_from_shield_y, 0);
	            
	            // invert previous direction
	            final float ipdir_x = -prev_debril.dir_x;
	            final float ipdir_y = -prev_debril.dir_y;
	            
	            final float A = vec_from_shield_y;
	            final float B = -vec_from_shield_x;
	            final float D = A * ipdir_x + B * ipdir_y;
	            
	            final float ndir_x = ipdir_x - 2 * A * D;
	            final float ndir_y = ipdir_y - 2 * B * D;
	            
	            debril.dir_x = ndir_x;
	            debril.dir_y = ndir_y;
	            
				debril.speed += GameLoopThread.ACCEL_PER_LOGIC * 2;
				if ( debril.speed > GameLoopThread.MAX_SPEED_LOGIC )
				{
					debril.speed = GameLoopThread.MAX_SPEED_LOGIC;
				}
				
				
				// this moves the debril the "extra" that it has to move after
				// adjusting the direction
				if ( Travel_dist < 0 )
				{
					// XXX after test, this happens, don't know if move or not the debril
//					System.out.println("WTF too big move-------------------------------------");
//					debril.x += debril.dir_x * (debril.speed);
//					debril.y += debril.dir_y * (debril.speed);
				}
				else
				{
					debril.x += debril.dir_x * (debril.speed - Travel_dist);
					debril.y += debril.dir_y * (debril.speed - Travel_dist);
				}
				
				ship.shield_active = true;
				ship.shield_counter = Math.round(LPS)/2;
				
				view.soundPool.play(view.shieldHitSoundId, 0.8f, 0.8f, 0, 0, 1.0f);
			}
		}
		
		
    }

	@Override
    public void run()
    {
//    	long ticksPS = 1000 / FPS;
//		long sleepTime;
        long current_time = System.currentTimeMillis();
        long accumulator = 0;
        
        int fps_count = 0;
        long fps_start = fps_count;
        long last_fps = 0;
        
        GameState st = GameState.Init;
        
    	shipRect.set(0, 0, GameResources.shipMask.getWidth(), GameResources.shipMask.getHeight());
    	debrilRect.set(0, 0, GameResources.debrilMask.getWidth(), GameResources.debrilMask.getHeight());

    	while (running)
    	{
    		Canvas c = null;
    		
    		final long new_time = System.currentTimeMillis();
    		final long fps_time = new_time - fps_start;
    		if ( fps_time >= 1000 )
    		{
//    			    System.out.println("FPS: " + fps_count + " in " + fps_time + " ms");
    			last_fps = fps_count;
    			fps_start = new_time;
    			fps_count = 0;
    		}
    		
    		final long frame_time = new_time - current_time;
    		// if ( frame_time > xxx ) frame_time = xxx limit;
    		current_time = new_time;

    		switch ( st )
    		{
    			case Init:
    				st = GameState.EnterStart;
    				configureSize(0, 0);
    				// fall throught
    				
    			case EnterStart:
    				pressed = false;
    				states.resetShip();
	    			this.calculateNewDirectionsAndSpeeds();
    				st = GameState.ReadyToStart;
    				// fall throught
    				
	    		case ReadyToStart:
	    		{
	    			accumulator += frame_time;
	    			
	    			while ( accumulator >= DELAY_BETWEEN_LOGICS )
	    			{
	    				states.swapStates();
	    				
	    				// force shield
	    				states.current.ship.shield = true;
	    				if ( states.prev.ship.shield_active )
	    				{
	    					states.current.ship.shield_counter = states.prev.ship.shield_counter - 1;
	    					states.current.ship.shield_active = states.current.ship.shield_counter > 0;
	    				}
	    				else states.current.ship.shield_active = false;
	    				
	    				debrilMoveLogic(states.prev.debrils, states.current.debrils);
	    				shieldCollisionLogic(states.current.ship, states.current.debrils, states.prev.debrils);
	    				
	    				accumulator -= DELAY_BETWEEN_LOGICS;
	    			}
	    			
	    			final float rat = (float)accumulator/DELAY_BETWEEN_LOGICS; 
	    			states.draw_state.reset();
	    			states.interpolDebrils(rat);
	    			states.interpolShip(rat);
	    			
	    			states.interpolParticles(frame_time);
	    			
	    			states.draw_state.alive_time = 0;
	    			states.draw_state.last_fps = last_fps;
	    			states.draw_state.addTop( GameResources.begin_message,
	    									  -(GameResources.begin_message.getWidth()/2),
	    									  -(GameResources.begin_message.getHeight()*2));
	
	    			if ( pressed )
	    			{
	    				pressed = false;
	    				st = GameState.Game;
	    				states.resetShip();
	    				// XXX hack to shield always on
//	    				states.current.ship.shield = true;
	    				accumulator = 0;
	    				current_time = System.currentTimeMillis();
	    				fps_start = current_time;
	    			}
	    			
	    		}
    				break;
    			
	    		case Game:
	    		{
	    			accumulator += frame_time;
	    			
	    			while ( accumulator >= DELAY_BETWEEN_LOGICS )
	    			{
	    				states.swapStates();
	    				
	    				final Ship ship = states.current.ship;
	    				
	    				ship.shield = states.prev.ship.shield;
	    				
	    				if ( states.prev.ship.shield_active )
	    				{
	    					ship.shield_counter = states.prev.ship.shield_counter - 1;
	    					ship.shield_active = ship.shield_counter > 0;
	    				}
	    				else ship.shield_active = false;
	    				
	    				shipMoveLogic(states.prev.ship, ship);
	
	    				debrilMoveLogic(states.prev.debrils, states.current.debrils);
	    				
	    				if ( ship.shield )
	    				{
	    					shieldCollisionLogic(ship, states.current.debrils, states.prev.debrils);
	    				}
//	    				else // enable "else" if ship explodes with shield (better find the logic error)
	    				{
		    				if ( shipCollisionLogic(ship, states.current.debrils) )
		    				{
		    					final Ship pship = states.prev.ship;
		    					final float dx = pship.dir_x * pship.dir_x;
		    					final float dy = pship.dir_y * pship.dir_y;
		    					final float sspeed = (float)Math.sqrt(dx + dy);
		    					
		    					states.draw_state.addExplosion(ship.x, ship.y, pship.dir_x/sspeed, pship.dir_y/sspeed, (float)Math.min(MAX_SPEED/4, sspeed*(LPS/4)));
		    					view.soundPool.play(view.explosionSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
		    					st = GameState.EnterDeath;
		    					pressed = false;
		    				}
	    				}
	    				
	    				accumulator -= DELAY_BETWEEN_LOGICS;
	    			}
	    			
	    			final float rat = (float)accumulator/DELAY_BETWEEN_LOGICS; 
	    			states.draw_state.reset();
	    			states.interpolShip(rat);
	    			states.interpolDebrils(rat);
	    			states.interpolParticles(frame_time);
	    			
	    			states.draw_state.alive_time += frame_time;
	    			states.draw_state.last_fps = last_fps;
	    			
	    		}
	    			break;
    			
    			/*
    			 * Sets all debrils speed limits and acceleration so they
    			 * reduce to the minimum.
    			 */
	    		case EnterDeath:
	    		{
	    			final Debril[] debrils = states.current.debrils;
	    			final int e = debrils.length;
	    			for ( int i = 0; i < e; i++ )
	    			{
	    				debrils[i].max_speed = REDUCED_SPEED_LOGIC;
	    				debrils[i].min_speed = REDUCED_SPEED_LOGIC;
	    				if ( debrils[i].speed > debrils[i].max_speed )
	    	    		{
	    					debrils[i].acceleration = -ACCEL_PER_LOGIC/4;
	    	    		}
	    	    		else if ( debrils[i].speed < debrils[i].min_speed )
	    	    		{
	    	    			debrils[i].acceleration = ACCEL_PER_LOGIC/4;
	    	    		}
	    	    		else
	    	    		{
	    	    			debrils[i].acceleration = 0;
	    	    		}
	    			}
	    			
	    			st = GameState.ReducingDeath;
	    		}	
	    			// fall through
	    		
	    		/*
	    		 * This waited until all the debrils were at the minimum speed
	    		 * TODO this could be removed, currently unwanted
	    		 */
	    		case ReducingDeath:
	    		{
    				boolean reduced = false;
	    			final Debril[] debrils = states.current.debrils;
	    			final int e = debrils.length;
	    			for ( int i = 0; i < e; i++ )
	    			{
	    				if ( debrils[i].acceleration != 0 )
	    				{
	    					reduced = true;
	    					break;
	    				}
	    			}
	    			
	    			if ( ! reduced )
	    			{
	    				st = GameState.Death;
	    			}
	    		}
	    			// fall through
	    			
	    		case Death:
	    			accumulator += frame_time;
	    			
	    			while ( accumulator >= DELAY_BETWEEN_LOGICS )
	    			{
	    				states.swapStates();
	    				
	    				debrilMoveLogic(states.prev.debrils, states.current.debrils);
	    				
	    				accumulator -= DELAY_BETWEEN_LOGICS;
	    			}
	    			
	    			final float rat = (float)accumulator/DELAY_BETWEEN_LOGICS; 
	    			
	    			states.draw_state.reset();
	    			states.interpolDebrils(rat);
	    			states.interpolParticles(frame_time);
	    			
	    			states.draw_state.last_fps = last_fps;
	    			states.draw_state.addTop( GameResources.death_message,
											  -(GameResources.death_message.getWidth()/2),
											  -(GameResources.death_message.getHeight()*2));
	
//	    			if ( st == GameState.Death && pressed )
    				if ( pressed )
	    			{
	    				st = GameState.EnterStart;
	    			}
	    			
	    			break;
    		} // switch
            
    		try
    		{
    			c = view.getHolder().lockCanvas();
    			if ( c != null )
    			{
    				synchronized (view.getHolder())
    				{
    					view.drawState(c, states.draw_state);
    				}
    			}
    		}
    		finally
    		{
    			if (c != null)
    			{
    				view.getHolder().unlockCanvasAndPost(c);
    			}
    		}
    		
    		++fps_count;

//            sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
//            
//            try
//            {
//            	if ( sleepTime > 0 )
//            		sleep(sleepTime);
//            	else
//            		sleep(10);
//            } catch (Exception e) {}

    	}
    }
	
	final public void calculateNewDirectionsAndSpeeds()
	{
    	final double pi2 = 2 * Math.PI;
    	
    	final Debril[] debrils = states.current.debrils;
    	final int e = debrils.length;
    	for ( int n = 0; n < e; n++ )
    	{
    		Debril d = debrils[n];
    		
    		final float dirangle = (float)(Math.random() * pi2);
    		d.dir_x = (float)Math.cos(dirangle);
    		d.dir_y = (float)Math.sin(dirangle);

    		d.min_speed = REDUCED_SPEED_LOGIC;
    		d.max_speed = (float)Math.random()* (MAX_SPEED_LOGIC/2) + MAX_SPEED_LOGIC/2;
    		if ( d.speed > d.max_speed )
    		{
    			// -ACCEL_PER_LOGIC was wrong, because it would slow down to minimum!
//    			d.acceleration = -ACCEL_PER_LOGIC;
    			d.acceleration = 0;
    			d.speed = d.max_speed;
    		}
    		else
    		{
    			d.acceleration = ACCEL_PER_LOGIC;
    		}
    	}
	}

	final public void configureSize(final int width, final int height)
	{
		float dist_start = 150f //width/3
    	     ,dist_range = 150f //width/2;
    	     ;
    	double angle, dist;
    	
    	double pi2 = 2 * Math.PI;

    	states.resetShip();
    	
    	final Debril[] debrils = states.current.debrils;
    	final int e = debrils.length;
    	for ( int n = 0; n < e; n++ )
    	{
    		Debril d = debrils[n];
    		
    		angle = Math.random() * pi2;
    		dist = Math.random() * dist_range + dist_start;
    		
    		d.x = (float)(Math.cos(angle)*dist);
    		d.y = (float)(Math.sin(angle)*dist);
    		
    		final float dirangle = (float)(Math.random() * pi2);
    		d.dir_x = (float)Math.cos(dirangle);
    		d.dir_y = (float)Math.sin(dirangle);
    		
    		d.min_speed = REDUCED_SPEED_LOGIC;
    		d.max_speed = (float)Math.random()* (MAX_SPEED_LOGIC/2) + MAX_SPEED_LOGIC/2;
    		d.speed = 0;
    		d.acceleration = ACCEL_PER_LOGIC;
    	}
    	
    	states.copyCurrentToPrev();
		
	}

	public void onDown(float prev_x, float prev_y)
	{
		pressed = true;
	}

}
