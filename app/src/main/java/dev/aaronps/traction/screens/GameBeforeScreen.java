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

/**
 *
 * @author krom
 */
public class GameBeforeScreen implements Screen
{
    public void enter()
    {
        BackgroundStarsParticleSystem.slowmo = true;
        Res.alive_time_number.value = 0;
        GAME.enterLevel(0);
    }

    public Screen logic(float time)
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
            
            Screens.GAME_PLAYING.enter();
            return Screens.GAME_PLAYING;
        }
        
        InputManager.noMove();
        
        GAME.beginStep();
        GAME.shipLogic(time);
        GAME.debrilMoveLogic(time);
        GAME.shieldCollisionLogic(time);
        
        return this;
    }

    public void interpol(float time)
    {
        BackgroundStarsParticleSystem.logic(time);
        SparkParticleSystem.logic(time);
        BitmapExplosionParticleSystem.logic(time);
    }

    public void drawGame(Canvas c)
    {
        final GameScreen gs = Screens.GAME_PLAYING;
        BackgroundStarsParticleSystem.draw(c);
        
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
        
        c.drawBitmap(s.shield_active ? Res.ship_aura_active : Res.ship_aura
                    ,s.x - Res.ship_aura_offset_x
                    ,s.y - Res.ship_aura_offset_y, null);
        
        SparkParticleSystem.draw(c);
        BitmapExplosionParticleSystem.draw(c);
    }

    public void drawUI(Canvas c)
    {
        Res.button_pause.draw(c);
        Res.begin_message.draw(c);
        Res.fps_number.draw(c);
        Res.alive_time_number.draw(c);
    }
    
}
