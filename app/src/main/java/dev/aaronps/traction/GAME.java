
package dev.aaronps.traction;

import android.graphics.Rect;
import dev.aaronps.traction.gamelayers.BitmapExplosionParticleSystem;
import dev.aaronps.traction.gamelayers.SparkParticleSystem;
import java.util.Random;

/**
 *
 * @author krom
 */
public class GAME
{
    public static State old_state = null;
    public static State new_state = null;
    
    private static InputManager.MoveCommand moveCommand = null;
    private static final Rect shipRect = new Rect(0,0,32,32);
    private static final Rect deRect = new Rect(0,0,16,16);
    private static final Rect intersectRect = new Rect();
    private static final Random rnd = new Random();
    
    public static void init()
    {
        if ( old_state == null )
        {
            old_state = new State();
            new_state = new State();
            moveCommand = new InputManager.MoveCommand();
        }
    }
    
    public static void enterLevel(final int level)
    {
        new_state.ship.x = new_state.ship.y = 0;
        initRandom(Config.MAX_DEBRILS);
//        initRandom(5);
    }
    
    private static void initRandom(final int ndebrils)
    {
        float dist_start = 150f //width/3
            , dist_range = 150f //width/2;
            ;
        double angle, dist;

        double pi2 = 2 * Math.PI;

        final Debril[] debrils = new_state.debrils;
        final int e = ndebrils;
        new_state.count = e;
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
            d.speed = d.min_speed;
            d.acceleration = Config.ACCEL_PER_SECOND;

        }
    }

    
    public static void beginStep()
    {
        final State s = old_state;
        old_state = new_state;
        new_state = s;
    }
    
    public static void shipLogic(final float time)
    {
        
        final Ship oship = old_state.ship;
        final Ship ship = new_state.ship;
        InputManager.getMoveCommand(moveCommand);
        
        ship.dir_x = moveCommand.dir_x;
        ship.dir_y = moveCommand.dir_y;
        ship.speed = moveCommand.speed;

        ship.shield = oship.shield;

        // shield logic
        if ( oship.shield_active )
        {
            ship.shield_counter = oship.shield_counter - 1;
            ship.shield_active = ship.shield_counter > 0;
        }
        else
        {
            ship.shield_active = false;
        }
        
        // move logic
        final float stime = moveCommand.speed; // oship.speed * time;
        ship.x = oship.x + moveCommand.dir_x * stime;
        ship.y = oship.y + moveCommand.dir_y * stime;

        // clip logic
             if (ship.x < Config.SHIP_MOVE_AREA.left)  ship.x = Config.SHIP_MOVE_AREA.left;
        else if (ship.x > Config.SHIP_MOVE_AREA.right) ship.x = Config.SHIP_MOVE_AREA.right;

             if (ship.y < Config.SHIP_MOVE_AREA.top)    ship.y = Config.SHIP_MOVE_AREA.top;
        else if (ship.y > Config.SHIP_MOVE_AREA.bottom) ship.y = Config.SHIP_MOVE_AREA.bottom;

    }
    
    public static void debrilMoveLogic(final float time)
    {
        final Debril[] old_array = old_state.debrils;
        final Debril[] new_array = new_state.debrils;
        final int e = old_state.count;
        new_state.count = e;
        for (int n = 0; n < e; n++)
        {
            final Debril oldd = old_array[n];
            final Debril newd = new_array[n];

            newd.max_speed = oldd.max_speed;
            newd.min_speed = oldd.min_speed;

            if ( oldd.acceleration != 0 )
            {
                newd.speed = oldd.speed + oldd.acceleration * time;
                if (newd.speed >= newd.max_speed && oldd.acceleration > 0)
                {
                    newd.speed = newd.max_speed;
                    newd.acceleration = 0;
                }
                else if (newd.speed <= newd.min_speed && oldd.acceleration < 0)
                {
                    newd.speed = newd.min_speed;
                    newd.acceleration = 0;
                }
                else
                {
                    newd.acceleration = oldd.acceleration;
                }
            }
            else
            {
                newd.speed = oldd.speed;
                newd.acceleration = 0;
            }
            /*
             * s = ut + 0.5at^2
             * 
             * s: distance to travel
             * t: time
             * a: acceleration
             * 
             */

            final float half_accel = newd.acceleration / 2;
            final float stime = oldd.speed * time;
            final float timesq = time * time;
            final float astime = half_accel * timesq;
            
            newd.x = oldd.x + oldd.dir_x * stime + oldd.dir_x * astime;
            newd.y = oldd.y + oldd.dir_y * stime + oldd.dir_y * astime;

            if (   (newd.x <= Config.WORLD_MIN_X && oldd.dir_x < 0)
                || (newd.x >= Config.WORLD_MAX_X && oldd.dir_x > 0) 
                || (newd.y <= Config.WORLD_MIN_Y && oldd.dir_y < 0)
                || (newd.y >= Config.WORLD_MAX_Y && oldd.dir_y > 0) )
            {
                final float nx = (old_state.ship.x - Config.DEBRIL_ATTACK_RAD + rnd.nextFloat() * Config.DEBRIL_ATTACK_DIA) - newd.x;
                final float ny = (old_state.ship.y - Config.DEBRIL_ATTACK_RAD + rnd.nextFloat() * Config.DEBRIL_ATTACK_DIA) - newd.y;
                
                final float dis = (float)Math.sqrt(nx*nx + ny*ny);
                
                newd.dir_x = nx / dis;
                newd.dir_y = ny / dis;
            }
            else
            {
                newd.dir_x = oldd.dir_x;
                newd.dir_y = oldd.dir_y;
            }
            
//            newd.x = oldd.x + oldd.dir_x * stime + oldd.dir_x * astime;
//            if (   (newd.x <= Config.WORLD_MIN_X && oldd.dir_x < 0)
//                || (newd.x >= Config.WORLD_MAX_X && oldd.dir_x > 0) )
//            {
//                newd.dir_x = -oldd.dir_x;
//            }
//            else
//            {
//                newd.dir_x = oldd.dir_x;
//            }
//
//            newd.y = oldd.y + oldd.dir_y * stime + oldd.dir_y * astime;
//            if (   (newd.y <= Config.WORLD_MIN_Y && oldd.dir_y < 0)
//                || (newd.y >= Config.WORLD_MAX_Y && oldd.dir_y > 0) )
//            {
//                newd.dir_y = -oldd.dir_y;
//            }
//            else
//            {
//                newd.dir_y = oldd.dir_y;
//            }
        }
    }
    
    public static boolean shipCollisionLogic()
    {
        final Ship ship = new_state.ship;
        final Debril[] debrils = new_state.debrils;
        final Rect ship_rect = shipRect;
        final Rect debril_rect = deRect;
        final Rect intersectionRect = intersectRect;

        ship_rect.offsetTo( (int) ship.x - Res.ship_offset_x,
                            (int) ship.y - Res.ship_offset_y);

        final int e = new_state.count;
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
                    if ( InputManager.working_mode == 0 ) // direct
                    {
                        BitmapExplosionParticleSystem.add(
                                Math.round(ship.x),
                                Math.round(ship.y),
                                ship.dir_x,
                                ship.dir_y,
                                2.0f);
                    }
                    else // joystick
                    {
                        BitmapExplosionParticleSystem.add(
                                Math.round(ship.x),
                                Math.round(ship.y),
                                ship.dir_x,
                                ship.dir_y,
                                ship.speed*8);
                    }

                    BitmapExplosionParticleSystem.add(
                            Math.round(cur.x),
                            Math.round(cur.y),
                            cur.dir_x,
                            cur.dir_y,
                            (float) Math.min(Config.MAX_SPEED / 4, cur.speed));
                    
                    
//                    SparkParticleSystem.addSpark(cur.x, cur.y, cur.dir_x, cur.dir_y);
                    
                    debrils[n] = debrils[e-1];
                    debrils[e-1] = cur;
                    new_state.count -= 1;

                    // volume shall be 1.0
                    SoundManager.player.play(Res.explosionSound);
                    return true;
                }
            }
        }

        return false;
    }
    
    public static void shieldCollisionLogic(final float time)
    {
        final Ship ship = new_state.ship;
        final Debril[] debrils = new_state.debrils;
        final Debril[] prev_debrils = old_state.debrils;
        final float shield_x = ship.x;
        final float shield_y = ship.y;

        final int e = new_state.count;
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

                SparkParticleSystem.addSpark(shield_x + v_from_shield_x * Config.SHIELD_RADII,
                                             shield_y + v_from_shield_y * Config.SHIELD_RADII,
                                             v_from_shield_x, v_from_shield_y);

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
    
}
