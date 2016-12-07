package dev.aaronps.traction.screens;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.Debril;
import dev.aaronps.traction.GAME;
import dev.aaronps.traction.InputManager;
import dev.aaronps.traction.Res;
import dev.aaronps.traction.Ship;
import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;
import dev.aaronps.traction.gamelayers.BitmapExplosionParticleSystem;
import dev.aaronps.traction.gamelayers.SparkParticleSystem;
import dev.aaronps.traction.gamelayers.ThrustParticleSystem;

/**
 *
 * @author krom
 */
public class GameScreen implements Screen
{
    
    public void init()
    {
        BackgroundStarsParticleSystem.init();
        SparkParticleSystem.init();
        BitmapExplosionParticleSystem.init();
        ThrustParticleSystem.init();
    }
    
    public void enter()
    {
        BackgroundStarsParticleSystem.slowmo = false;
    }
    
    public final Screen logic(final float time)
    {
        if ( InputManager.wasPressed() )
        {
            InputManager.resetPress();
            final float x = InputManager.pointer_x / Config.screen_x_ratio;
            final float y = InputManager.pointer_y / Config.screen_y_ratio;
            if ( Res.button_pause.dst.contains(x, y) )
            {
                Screens.GAME_PAUSE.resume_to = this;
                Screens.GAME_PAUSE.enter();
                return Screens.GAME_PAUSE;
            }
        }
        
        ThrustParticleSystem.active = InputManager.pressing
                                    || BackgroundStarsParticleSystem.time_rate < 1.0f;

        GAME.beginStep();
        GAME.shipLogic(time);
        GAME.debrilMoveLogic(time);
        if ( GAME.shipCollisionLogic() )
        {
            Screens.GAME_DEAD.enter();
            return Screens.GAME_DEAD;
        }

        return this;
    }

    public final void interpol(final float time)
    {
        BackgroundStarsParticleSystem.logic(time);
        SparkParticleSystem.logic(time);
        BitmapExplosionParticleSystem.logic(time);
        ThrustParticleSystem.setShipLocation(GAME.new_state.ship.x, GAME.new_state.ship.y);
        ThrustParticleSystem.logic(time);
        
        Res.alive_time_number.value += (int)(time * 1000);
    }
    
    public final void drawGame(final Canvas c)
    {
        BackgroundStarsParticleSystem.draw(c);
        ThrustParticleSystem.draw(c);
        
        final Ship s = GAME.new_state.ship;
        c.drawBitmap(Res.ship, s.x - Res.ship_offset_x
                             , s.y - Res.ship_offset_y, null);
        
        final Bitmap b = Res.debril;
        final Debril[] debrils = GAME.new_state.debrils;
        for ( int t = GAME.new_state.count; t > 0; )
        {
            Debril d = debrils[--t];
            c.drawBitmap(b, d.x - Res.debril_offset_x,
                            d.y - Res.debril_offset_y, null);
        }
        
        if ( s.shield )
        {
            c.drawBitmap(s.shield_active ? Res.ship_aura_active : Res.ship_aura
                        ,s.x - Res.ship_aura_offset_x
                        ,s.y - Res.ship_aura_offset_y, null);
        }
        
        SparkParticleSystem.draw(c);
        BitmapExplosionParticleSystem.draw(c);
        
    }
    
    public final void drawUI(final Canvas c)
    {
        Res.button_pause.draw(c);
        Res.fps_number.draw(c);
        Res.alive_time_number.draw(c);
    }
    
}
