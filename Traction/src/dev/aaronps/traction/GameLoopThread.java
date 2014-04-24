package dev.aaronps.traction;

import java.util.InputMismatchException;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Process;

public class GameLoopThread extends Thread
{
	
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
    
    private GameState logicState = GameState.Init;

    private final InputManager.MoveCommand moveCommand = new InputManager.MoveCommand();
    
    
    public GameLoopThread(GameView view)
    {
    	this.view = view;
    	
    	states = new States(Config.MAX_DEBRIL_COUNT);
    }

    public void setRunning(boolean run)
    {
    	running = run;
    }
    
    private static final void shipMoveLogic(final Ship pship, final Ship ship)
    {
    	ship.x = pship.x + pship.dir_x * pship.speed;
		ship.y = pship.y + pship.dir_y * pship.speed;
		
		     if ( ship.x < Config.SHIP_MOVE_AREA.left ) { ship.x = Config.SHIP_MOVE_AREA.left; }
		else if ( ship.x > Config.SHIP_MOVE_AREA.right) { ship.x = Config.SHIP_MOVE_AREA.right; }
		
		     if ( ship.y < Config.SHIP_MOVE_AREA.top) { ship.y = Config.SHIP_MOVE_AREA.top; }
		else if ( ship.y > Config.SHIP_MOVE_AREA.bottom) { ship.y = Config.SHIP_MOVE_AREA.bottom; }
		
		ship.dir_x = ship.dir_y = ship.speed = 0;
    }
    
    private static final void debrilMoveLogic(final Debril[] prev_array, final Debril[] cur_array)
    {
    	final int e = prev_array.length;
    	for ( int n = 0; n < e; n++ )
		{
			final Debril prev = prev_array[n];
			final Debril cur = cur_array[n];
			
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
			if (   (cur.x <= Config.WORLD_MIN_X && prev.dir_x < 0)
				|| (cur.x >= Config.WORLD_MAX_X && prev.dir_x > 0) )
			{
				cur.dir_x = -prev.dir_x;
			}
			else
			{
				cur.dir_x = prev.dir_x;
			}
			
			cur.y = prev.y + prev.dir_y * prev.speed + prev.dir_y * half_accel;
			if (   (cur.y <= Config.WORLD_MIN_Y && prev.dir_y < 0) 
				|| (cur.y >= Config.WORLD_MAX_Y && prev.dir_y > 0) )
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
    	
    	final int e = debrils.length;
		for ( int n = 0; n < e; n++ )
		{
			final Debril debril = debrils[n];
			final float shield_debril_dist_x = shield_x - debril.x;
			final float shield_debril_dist_y = shield_y - debril.y;
			final float shield_debril_dist_sq = shield_debril_dist_x * shield_debril_dist_x + shield_debril_dist_y * shield_debril_dist_y;

			if ( shield_debril_dist_sq <= Config.COMBINED_RADII_SQ ) 
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
	                
	                final float nx = shield_x + from_shield_to_debril_x * Config.COMBINED_RADII;
	                final float ny = shield_y + from_shield_to_debril_y * Config.COMBINED_RADII;

	                debril.x = nx;
	                debril.y = ny;
	                debril.dir_x = from_shield_to_debril_x;
	                debril.dir_y = from_shield_to_debril_y;
	                
	                ship.shield_active = true;
					ship.shield_counter = Math.round(Config.LPS)/2;
	                view.soundPool.play(view.shieldHitSoundId, 0.8f, 0.8f, 0, 0, 1.0f);

					continue;
				}
				
				final float C_len = (float)Math.sqrt(C_x*C_x + C_y*C_y);
				final float F = (C_len*C_len) - (D_col*D_col);
				final float T = Config.COMBINED_RADII_SQ - F;
				
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
				
				final float vec_from_shield_x = (debril.x - shield_x) / Config.COMBINED_RADII;
	            final float vec_from_shield_y = (debril.y - shield_y) / Config.COMBINED_RADII;
	            
	            states.draw_state.addSpark( shield_x + vec_from_shield_x * Config.SHIELD_RADII,
	            							shield_y + vec_from_shield_y * Config.SHIELD_RADII,
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
	            
				debril.speed += Config.ACCEL_PER_LOGIC * 2;
				if ( debril.speed > Config.MAX_SPEED_LOGIC )
				{
					debril.speed = Config.MAX_SPEED_LOGIC;
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
				ship.shield_counter = Math.round(Config.LPS)/2;
				
				view.soundPool.play(view.shieldHitSoundId, 0.8f, 0.8f, 0, 0, 1.0f);
			}
		}
		
		
    }

	@Override
    public void run()
    {
        int fps_count = 0;
        long fps_count_start_time = System.currentTimeMillis();
        long last_second_fps_count = 0;
        
    	shipRect.set(0, 0, GameResources.shipMask.getWidth(), GameResources.shipMask.getHeight());
    	debrilRect.set(0, 0, GameResources.debrilMask.getWidth(), GameResources.debrilMask.getHeight());
    	
    	while (running)
    	{
    	    Canvas c = null;
    		try
    		{
    		    c = view.getHolder().lockCanvas();
    		    if ( c != null )
    		    {
//    		        synchronized (view.getHolder())
//    		        {
            		final long this_frame_start =  System.currentTimeMillis();
            		
            		final long fps_time = this_frame_start - fps_count_start_time;
            		if ( fps_time >= 1000 )
            		{
            			last_second_fps_count = fps_count;
            			fps_count_start_time = this_frame_start;
            			fps_count = 0;
            		}
            		
//            		final long last_frame_time = this_frame_start - last_frame_start;
//            		accumulator += last_frame_time;
//            		doLogic( (int)(accumulator / DELAY_BETWEEN_LOGICS) );
//            		accumulator %= DELAY_BETWEEN_LOGICS;
//            		interpol( accumulator/(float)DELAY_BETWEEN_LOGICS, last_frame_time );
            		
            		doLogic();
            		interpol( 1.0f, Config.DELAY_BETWEEN_LOGICS );
            		
                    states.draw_state.last_fps = last_second_fps_count;
            		
					view.drawState(c, states.draw_state);
//    				} // synchronized()
					++fps_count;
    			}
    		}
    		finally
    		{
    			if (c != null)
    			{
    				view.getHolder().unlockCanvasAndPost(c);
    			}
    		}
    		
    		
    	}
    }
	
	final void doLogic()
	{
	    switch ( logicState )
        {
            case Init:
                logicState = GameState.EnterStart;
                ThrustParticleSystem.active = false;
                BackgroundStarsParticleSystem.slowmo = true;
                configureSize(0, 0);
                // fall through
                
            case EnterStart:
                InputManager.resetPress();
                states.resetShip();
                // XXX may need to reset ship move command here or later?
                this.calculateNewDirectionsAndSpeeds();
                logicState = GameState.ReadyToStart;
                // fall through
                
            case ReadyToStart:
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
                
                if ( InputManager.wasPressed() )
                {
                    InputManager.resetPress();
                    logicState = GameState.Game;
                    states.resetShip();
                    InputManager.resetPress();
                    // XXX hack to shield always on
//                  states.current.ship.shield = true;

                    BackgroundStarsParticleSystem.slowmo = false;
                    ThrustParticleSystem.active = true;
                }
                
            }
                break;
            
            case Game:
            {
                {
                    InputManager.getMoveCommand( moveCommand );
                    states.current.ship.dir_x = moveCommand.dir_x;
                    states.current.ship.dir_y = moveCommand.dir_y;
                    states.current.ship.speed = moveCommand.speed;
                }
                
                
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
//                  else // enable "else" if ship explodes with shield (better find the logic error)
                if ( shipCollisionLogic(ship, states.current.debrils) )
                {
                    final Ship pship = states.prev.ship;
                    final float dx = pship.dir_x * pship.dir_x;
                    final float dy = pship.dir_y * pship.dir_y;
                    final float sspeed = (float)Math.sqrt(dx + dy);
                    
                    states.draw_state.addExplosion(ship.x, ship.y, pship.dir_x/sspeed, pship.dir_y/sspeed, (float)Math.min(Config.MAX_SPEED/4, sspeed*(Config.LPS/4)));
                    view.soundPool.play(view.explosionSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                    logicState = GameState.EnterDeath;
                    InputManager.resetPress();
                }
            }
                break;
            
            /*
             * Sets all debrils speed limits and acceleration so they
             * reduce to the minimum.
             */
            case EnterDeath:
            {
                ThrustParticleSystem.active = false;
                BackgroundStarsParticleSystem.slowmo = true;
                final Debril[] debrils = states.current.debrils;
                final int e = debrils.length;
                for ( int i = 0; i < e; i++ )
                {
                    debrils[i].max_speed = Config.REDUCED_SPEED_LOGIC;
                    debrils[i].min_speed = Config.REDUCED_SPEED_LOGIC;
                    if ( debrils[i].speed > debrils[i].max_speed )
                    {
                        debrils[i].acceleration = -Config.ACCEL_PER_LOGIC/4;
                    }
                    else if ( debrils[i].speed < debrils[i].min_speed )
                    {
                        debrils[i].acceleration = Config.ACCEL_PER_LOGIC/4;
                    }
                    else
                    {
                        debrils[i].acceleration = 0;
                    }
                }
                
                logicState = GameState.ReducingDeath;
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
                    logicState = GameState.Death;
                }
            }
                // fall through
                
            case Death:
                states.swapStates();
                
                debrilMoveLogic(states.prev.debrils, states.current.debrils);
                
//              if ( st == GameState.Death && pressed )
                if ( InputManager.wasPressed() )
                {
                    logicState = GameState.EnterStart;
                }
                
                break;
        }
	}
	
	final void interpol(final float rate, final long last_frame_time)
	{
	    switch ( logicState )
	    {
	        case ReadyToStart:
                states.draw_state.reset();
                states.interpolDebrils(rate);
                states.interpolShip(rate);
                
                states.interpolParticles(last_frame_time);
                
                states.draw_state.alive_time = 0;
                states.draw_state.addTop( GameResources.begin_message,
                                          -(GameResources.begin_message.getWidth()/2),
                                          -(GameResources.begin_message.getHeight()*2));
	            break;
	        
	        case Game:
                states.draw_state.reset();
                states.interpolShip(rate);
                states.interpolDebrils(rate);
                states.interpolParticles(last_frame_time);
                
                states.draw_state.alive_time += last_frame_time;
                
                break;
                
	        case ReducingDeath:
	        case Death:
                states.draw_state.reset();
                states.interpolDebrils(rate);
                states.interpolParticles(last_frame_time);
                
                states.draw_state.addTop( GameResources.death_message,
                                          -(GameResources.death_message.getWidth()/2),
                                          -(GameResources.death_message.getHeight()*2));
                
                break;
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

    		d.min_speed = Config.REDUCED_SPEED_LOGIC;
    		d.max_speed = (float)Math.random()* (Config.MAX_SPEED_LOGIC/2) + Config.MAX_SPEED_LOGIC/2;
    		if ( d.speed > d.max_speed )
    		{
    			// -ACCEL_PER_LOGIC was wrong, because it would slow down to minimum!
//    			d.acceleration = -ACCEL_PER_LOGIC;
    			d.acceleration = 0;
    			d.speed = d.max_speed;
    		}
    		else
    		{
    			d.acceleration = Config.ACCEL_PER_LOGIC;
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
    		
    		d.min_speed = Config.REDUCED_SPEED_LOGIC;
    		d.max_speed = (float)Math.random()* (Config.MAX_SPEED_LOGIC/2) + Config.MAX_SPEED_LOGIC/2;
    		d.speed = 0;
    		d.acceleration = Config.ACCEL_PER_LOGIC;
    	}
    	
    	states.copyCurrentToPrev();
		
	}

}
