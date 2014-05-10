package dev.aaronps.traction;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;
import dev.aaronps.traction.gamelayers.ThrustParticleSystem;
import dev.aaronps.traction.ui.UIElement;
import dev.aaronps.traction.ui.UIImage;
import dev.aaronps.traction.ui.UINumber;

public class GameLoopThread extends Thread
{
    private static enum GameState
    {
        MainMenu, Init, EnterStart, ReadyToStart,
        Game, EnterDeath, ReducingDeath, Death;
    }

    private static final Rect shipRect = new Rect();
    private static final Rect deRect = new Rect();
    private static final Rect intersectRect = new Rect();

    private final GameView view;
    private final States states;
    private boolean running = false;
    
    private GameState logicState = GameState.MainMenu;

    private final InputManager.MoveCommand moveCommand = new InputManager.MoveCommand();

    private final UIElement start_button;
    private final UIElement config_button;
    private final UIElement exit_button;
    private final UIElement begin_message;
    private final UIElement death_message;
    
    private final UINumber fps_number;
    private final UINumber alive_time_number;
    
    public GameLoopThread(GameView view)
    {
        this.view = view;
        states = new States(Config.MAX_DEBRIL_COUNT);
        
        start_button  = new UIImage(Res.menu_play,   100, 200);
        config_button = new UIImage(Res.menu_config, 100, 350);
        exit_button   = new UIImage(Res.menu_exit,   100, 500);
        
        begin_message = new UIImage(Res.begin_message,
                        (480 / 2) - (Res.begin_message.getWidth() / 2),
                        (800 / 2) - (Res.begin_message.getHeight() * 2));
        
        death_message = new UIImage(Res.death_message,
                        (480 / 2) - (Res.death_message.getWidth() / 2),
                        (800 / 2) - (Res.death_message.getHeight() * 2));
        
        fps_number = new UINumber(0, 472, 0, 2);
        alive_time_number = new UINumber(3, 240, 0, 1);
    }

    public void setRunning(boolean run)
    {
        running = run;
    }

    private static void shipMoveLogic(  final Ship pship,
                                        final Ship ship,
                                        final float time)
    {
        final float stime = pship.speed * time;
        ship.x = pship.x + pship.dir_x * stime;
        ship.y = pship.y + pship.dir_y * stime;

             if (ship.x < Config.SHIP_MOVE_AREA.left)  ship.x = Config.SHIP_MOVE_AREA.left;
        else if (ship.x > Config.SHIP_MOVE_AREA.right) ship.x = Config.SHIP_MOVE_AREA.right;

             if (ship.y < Config.SHIP_MOVE_AREA.top)    ship.y = Config.SHIP_MOVE_AREA.top;
        else if (ship.y > Config.SHIP_MOVE_AREA.bottom) ship.y = Config.SHIP_MOVE_AREA.bottom;

        ship.dir_x = ship.dir_y = ship.speed = 0;
    }

    private static void debrilMoveLogic(final Debril[] prev_array,
                                        final Debril[] cur_array,
                                        final float time)
    {
        final int e = prev_array.length;
        for (int n = 0; n < e; n++)
        {
            final Debril prev = prev_array[n];
            final Debril cur = cur_array[n];

            cur.max_speed = prev.max_speed;
            cur.min_speed = prev.min_speed;

            if (prev.acceleration != 0)
            {
                cur.speed = prev.speed + prev.acceleration * time;
                if (cur.speed >= cur.max_speed && prev.acceleration > 0)
                {
                    cur.speed = cur.max_speed;
                    cur.acceleration = 0;
                }
                else if (cur.speed <= cur.min_speed && prev.acceleration < 0)
                {
                    // TODO bug, if the speed was lower when started to increase,
                    // it will keep at min_speed
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
            final float stime = prev.speed * time;
            final float timesq = time * time;
            final float astime = half_accel * timesq;

            cur.x = prev.x + prev.dir_x * stime + prev.dir_x * astime;
            if (   (cur.x <= Config.WORLD_MIN_X && prev.dir_x < 0)
                || (cur.x >= Config.WORLD_MAX_X && prev.dir_x > 0) )
            {
                cur.dir_x = -prev.dir_x;
            }
            else
            {
                cur.dir_x = prev.dir_x;
            }

            cur.y = prev.y + prev.dir_y * stime + prev.dir_y * astime;
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

    private boolean shipCollisionLogic(final Ship ship, final Debril[] debrils)
    {
        final Rect ship_rect = shipRect;
        final Rect debril_rect = deRect;
        final Rect intersectionRect = intersectRect;

        ship_rect.offsetTo( (int) ship.x - Res.ship_offset_x,
                            (int) ship.y - Res.ship_offset_y);

        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            final Debril cur = debrils[n];

            debril_rect.offsetTo( (int) cur.x - Res.debril_offset_x,
                                  (int) cur.y - Res.debril_offset_y);

            if (intersectionRect.setIntersect(ship_rect, debril_rect))
            {
                if (Res.shipMask.collidesWith(
                        intersectionRect.left - ship_rect.left,
                        intersectionRect.top - ship_rect.top,
                        intersectionRect.height(),
                        Res.debrilMask,
                        intersectionRect.left - debril_rect.left,
                        intersectionRect.top - debril_rect.top))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void shieldCollisionLogic(  final Ship ship,
                                        final Debril[] debrils,
                                        final Debril[] prev_debrils,
                                        final float time)
    {
        final float shield_x = ship.x;
        final float shield_y = ship.y;

        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            final Debril debril = debrils[n];
            final float sd_dist_x = shield_x - debril.x;
            final float sd_dist_y = shield_y - debril.y;
            final float shield_debril_dist_sq = sd_dist_x * sd_dist_x
                                              + sd_dist_y * sd_dist_y;

            if (shield_debril_dist_sq <= Config.COMBINED_RADII_SQ)
            {
                final Debril p_debril = prev_debrils[n];

                final float C_x = shield_x - p_debril.x;
                final float C_y = shield_y - p_debril.y;
                final float D_col = p_debril.dir_x * C_x + p_debril.dir_y * C_y;

                // the ball might be already inside the shield, if so, push it out
                if (D_col <= 0)
                {
                    float shield2debril_x = p_debril.x - shield_x;
                    float shield2debril_y = p_debril.y - shield_y;
                    final float fstd = (float) Math.sqrt( shield2debril_x * shield2debril_x
                                                        + shield2debril_y * shield2debril_y);
                    shield2debril_x /= fstd;
                    shield2debril_y /= fstd;

                    final float nx = shield_x + shield2debril_x * Config.COMBINED_RADII;
                    final float ny = shield_y + shield2debril_y * Config.COMBINED_RADII;

                    debril.x = nx;
                    debril.y = ny;
                    debril.dir_x = shield2debril_x;
                    debril.dir_y = shield2debril_y;

                    ship.shield_active = true;
                    ship.shield_counter = Math.round(Config.LPS) / 2;
                    SoundManager.player.play(Res.shieldHitSound);
                    continue;
                }

                final float C_len = (float) Math.sqrt(C_x * C_x + C_y * C_y);
                final float F = (C_len * C_len) - (D_col * D_col);
                final float T = Config.COMBINED_RADII_SQ - F;

                // if T < 0 no col
                if (T < 0)
                {
                    System.out.println("T < 0!!!");
                }

                final float extra_move = (float) Math.sqrt(T);
                final float Travel_dist = D_col - extra_move;

                // point of collision
                debril.x = p_debril.x + p_debril.dir_x * Travel_dist;
                debril.y = p_debril.y + p_debril.dir_y * Travel_dist;

                final float v_from_shield_x = (debril.x - shield_x) / Config.COMBINED_RADII;
                final float v_from_shield_y = (debril.y - shield_y) / Config.COMBINED_RADII;

                states.addSpark(shield_x + v_from_shield_x * Config.SHIELD_RADII,
                                shield_y + v_from_shield_y * Config.SHIELD_RADII,
                                v_from_shield_x, v_from_shield_y, 0);

                // invert previous direction
                final float ipdir_x = -p_debril.dir_x;
                final float ipdir_y = -p_debril.dir_y;

                final float A = v_from_shield_y;
                final float B = -v_from_shield_x;
                final float D = A * ipdir_x + B * ipdir_y;

                final float ndir_x = ipdir_x - 2 * A * D;
                final float ndir_y = ipdir_y - 2 * B * D;

                debril.dir_x = ndir_x;
                debril.dir_y = ndir_y;

                // this moves the debril the "extra" that it has to move after
                // adjusting the direction
                if (Travel_dist < 0)
                {
                    // XXX after test, this happens,
                    // don't know if move or not the debril
//                    System.out.println("WTF too big move-------------");
//                    debril.x += debril.dir_x * (debril.speed);
//                    debril.y += debril.dir_y * (debril.speed);
                }
                else
                {
                    final float stime = debril.speed * time;
                    debril.x += debril.dir_x * (stime - Travel_dist);
                    debril.y += debril.dir_y * (stime - Travel_dist);
                }

                ship.shield_active = true;
                ship.shield_counter = Math.round(Config.LPS) / 2;

                SoundManager.player.play(Res.shieldHitSound);
            }
        }
    }

    @Override
    public void run()
    {
        int fps_count = 0;
        long last_frame_start = System.nanoTime();
        long accumulator = -1;
        long fps_count_start = last_frame_start;
        long last_second_fps_count = 0;

        shipRect.set(0, 0, Res.shipMask.getWidth(), Res.shipMask.getHeight());
        deRect.set(0, 0, Res.debrilMask.getWidth(), Res.debrilMask.getHeight());

        final SurfaceHolder holder = view.getHolder();
        while (running)
        {
            final Canvas c = holder.lockCanvas();
            if (c != null)
            {
                try
                {
                    final long frame_start = System.nanoTime();
                    if (accumulator < 0)
                    {
                        last_frame_start = frame_start;
                        accumulator = 0;
                    }

                    final long fps_time = frame_start - fps_count_start;
                    if (fps_time >= 1000000000) // 1s in ns
                    {
                        last_second_fps_count = fps_count;
                        fps_count_start = frame_start;
                        fps_count = 0;
                    }

                    final long last_frame_time = frame_start - last_frame_start;

                    // from ns to s
                    final float lftime = last_frame_time / 1000000000f;
//                doLogic( lftime );
//                interpol( 1.0f, last_frame_time );

                    accumulator += last_frame_time;
                    int loopcount = 0;
                    while ( ++loopcount <= Config.MAX_LOGIC_LOOP
                            && accumulator >= Config.DELAY_BETWEEN_LOGICS )
                    {
                        doLogic(Config.LOGIC_FRAMETIME_S);
                        accumulator -= Config.DELAY_BETWEEN_LOGICS;
                    }

                    states.draw_state.last_fps = last_second_fps_count;
                    
                    interpol(1.0f, lftime);

                    view.drawState(c, states.draw_state);
                    ++fps_count;

                    last_frame_start = frame_start;
                }
                finally
                {
                    holder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    final void doLogic(final float time)
    {
        switch (logicState)
        {
            case MainMenu:
                BackgroundStarsParticleSystem.slowmo = true;
                break;

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
                if (states.prev.ship.shield_active)
                {
                    states.current.ship.shield_counter = states.prev.ship.shield_counter - 1;
                    states.current.ship.shield_active = states.current.ship.shield_counter > 0;
                }
                else
                {
                    states.current.ship.shield_active = false;
                }

                debrilMoveLogic(states.prev.debrils, states.current.debrils, time);
                shieldCollisionLogic(states.current.ship, states.current.debrils, states.prev.debrils, time);

                if (InputManager.wasPressed())
                {
                    logicState = GameState.Game;
                    states.resetShip();
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
                    InputManager.getMoveCommand(moveCommand);
                    states.current.ship.dir_x = moveCommand.dir_x;
                    states.current.ship.dir_y = moveCommand.dir_y;
                    states.current.ship.speed = moveCommand.speed / time;
                }

                ThrustParticleSystem.active = InputManager.wasPressed()
                                            || BackgroundStarsParticleSystem.time_rate < 1.0f;

                states.swapStates();

                final Ship ship = states.current.ship;

                ship.shield = states.prev.ship.shield;

                if (states.prev.ship.shield_active)
                {
                    ship.shield_counter = states.prev.ship.shield_counter - 1;
                    ship.shield_active = ship.shield_counter > 0;
                } else
                {
                    ship.shield_active = false;
                }

                shipMoveLogic(states.prev.ship, ship, time);

                debrilMoveLogic(states.prev.debrils, states.current.debrils, time);

                if (ship.shield)
                {
                    shieldCollisionLogic(ship, states.current.debrils, states.prev.debrils, time);
                }
                // enable "else" if ship explodes with shield(or find the error)
//                  else 
                if (shipCollisionLogic(ship, states.current.debrils))
                {
                    final Ship pship = states.prev.ship;
                    final float dx = pship.dir_x * pship.dir_x;
                    final float dy = pship.dir_y * pship.dir_y;
                    final float sspeed = (float) Math.sqrt(dx + dy);

                    states.addExplosion(ship.x, ship.y,
                                        pship.dir_x/sspeed, pship.dir_y/sspeed,
                                        (float) Math.min(Config.MAX_SPEED / 4,
                                        sspeed * (Config.LPS / 4)));
                    
                    // volume shall be 1.0
                    SoundManager.player.play(Res.explosionSound);
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
                for (int i = 0; i < e; i++)
                {
                    debrils[i].max_speed = Config.REDUCED_SPEED;
                    debrils[i].min_speed = Config.REDUCED_SPEED;
                    if (debrils[i].speed > debrils[i].max_speed)
                    {
                        debrils[i].acceleration = -Config.ACCEL_PER_SECOND / 4;
                    }
                    else if (debrils[i].speed < debrils[i].min_speed)
                    {
                        debrils[i].acceleration = Config.ACCEL_PER_SECOND / 4;
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
                for (int i = 0; i < e; i++)
                {
                    if (debrils[i].acceleration != 0)
                    {
                        reduced = true;
                        break;
                    }
                }

                if (!reduced)
                {
                    logicState = GameState.Death;
                }
            }
                // fall through

            case Death:
                states.swapStates();

//              debrilMoveLogic(states.prev.debrils, states.current.debrils, time);
//                if ( st == GameState.Death && pressed )
                if (InputManager.wasPressed())
                {
                    logicState = GameState.EnterStart;
                }

                break;
        }
    }

    final void interpol(final float rate, final float last_frame_time)
    {
        final DrawState ds = states.draw_state;
        switch (logicState)
        {
            case MainMenu:
                ds.reset();
                states.interpolParticles(last_frame_time);

                ds.addLayer(states.backgroundStars);

                ds.addUI(start_button);
                ds.addUI(config_button);
                ds.addUI(exit_button);
                
                fps_number.value = (int)ds.last_fps;
                ds.addUI(fps_number);
                
                break;

            case ReadyToStart:
                ds.reset();
                states.sprite_layer.reset();

                states.interpolDebrils(rate);
                states.interpolShip(rate);

                states.interpolParticles(last_frame_time);

                ds.addLayer(states.backgroundStars);
                ds.addLayer(states.explosions);
                ds.addLayer(states.thrustParticles);
                ds.addLayer(states.sprite_layer);
                ds.addLayer(states.sparks);

                states.draw_state.alive_time = 0;
                ds.addUI(begin_message);
                
                fps_number.value = (int)ds.last_fps;
                ds.addUI(fps_number);

                alive_time_number.value = 0;
                ds.addUI(alive_time_number);

                break;

            case Game:
                ds.reset();
                states.sprite_layer.reset();

                states.interpolShip(rate);
                states.interpolDebrils(rate);
                states.interpolParticles(last_frame_time);

                ds.alive_time += last_frame_time;

                ds.addLayer(states.backgroundStars);
                ds.addLayer(states.explosions);
                ds.addLayer(states.thrustParticles);
                ds.addLayer(states.sprite_layer);
                ds.addLayer(states.sparks);
                
                fps_number.value = (int)ds.last_fps;
                ds.addUI(fps_number);
                
                alive_time_number.value = (int)(ds.alive_time * 1000f);
                ds.addUI(alive_time_number);

                break;

            case ReducingDeath:
            case Death:
                ds.reset();
                states.sprite_layer.reset();

                states.interpolShip(rate);
                states.interpolDebrils(rate);
                states.interpolParticles(last_frame_time);

                ds.addLayer(states.backgroundStars);
                ds.addLayer(states.explosions);
                ds.addLayer(states.sprite_layer);

                ds.addUI(death_message);
                
                fps_number.value = (int)ds.last_fps;
                ds.addUI(fps_number);
                
                alive_time_number.value = (int)(ds.alive_time * 1000f);
                ds.addUI(alive_time_number);
                break;
        }
    }

    final public void calculateNewDirectionsAndSpeeds()
    {
        final double pi2 = 2 * Math.PI;

        final Debril[] debrils = states.current.debrils;
        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            Debril d = debrils[n];

            final float dirangle = (float) (Math.random() * pi2);
            d.dir_x = (float) Math.cos(dirangle);
            d.dir_y = (float) Math.sin(dirangle);

            d.min_speed = Config.REDUCED_SPEED;
            d.max_speed = (float) Math.random() * (Config.MAX_SPEED / 2)
                        + Config.MAX_SPEED / 2;
            if (d.speed > d.max_speed)
            {
                d.acceleration = 0;
                d.speed = d.max_speed;
            }
            else
            {
                d.acceleration = Config.ACCEL_PER_SECOND;
            }
        }
    }

    final public void configureSize(final int width, final int height)
    {
        float dist_start = 150f //width/3
                , dist_range = 150f //width/2;
                ;
        double angle, dist;

        double pi2 = 2 * Math.PI;

        states.resetShip();

        final Debril[] debrils = states.current.debrils;
        final int e = debrils.length;
        for (int n = 0; n < e; n++)
        {
            Debril d = debrils[n];

            angle = Math.random() * pi2;
            dist = Math.random() * dist_range + dist_start;

            d.x = (float) (Math.cos(angle) * dist);
            d.y = (float) (Math.sin(angle) * dist);

            final float dirangle = (float) (Math.random() * pi2);
            d.dir_x = (float) Math.cos(dirangle);
            d.dir_y = (float) Math.sin(dirangle);

            d.min_speed = Config.REDUCED_SPEED;
            d.max_speed = (float) Math.random() * (Config.MAX_SPEED / 2)
                        + Config.MAX_SPEED / 2;
            d.speed = 0;
            d.acceleration = Config.ACCEL_PER_SECOND;

        }

        states.copyCurrentToPrev();

    }

}
