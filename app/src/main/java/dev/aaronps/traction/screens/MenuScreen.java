package dev.aaronps.traction.screens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.util.Log;
import dev.aaronps.gameengine.Screen;
import dev.aaronps.traction.Config;
import dev.aaronps.traction.InputManager;
import dev.aaronps.traction.Res;
import dev.aaronps.traction.gamelayers.BackgroundStarsParticleSystem;
import dev.aaronps.traction.GAME;

/**
 *
 * @author krom
 */
public class MenuScreen implements Screen
{
    public Context ctx = null;

    public void enter()
    {
        BackgroundStarsParticleSystem.init();
        BackgroundStarsParticleSystem.slowmo = true;
        BackgroundStarsParticleSystem.setSlowSpeed();
    }

    public final Screen logic(final float time)
    {
        if ( InputManager.wasPressed() )
        {
            InputManager.resetPress();
            final float x = InputManager.pointer_x / Config.screen_x_ratio;
            final float y = InputManager.pointer_y / Config.screen_y_ratio;

            if ( Res.button_start.dst.contains(x, y) )
            {
                GAME.init();
                Screens.GAME_PLAYING.init();
                Screens.GAME_BEFORE.enter();
                return Screens.GAME_BEFORE;
            }
            else if ( Res.button_config.dst.contains(x, y) )
            {
                Screens.CONFIG.resume_to = this;
                return Screens.CONFIG;
            }
            else if ( Res.button_exit.dst.contains(x, y) )
            {
                final Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        }

        return this;
    }

    public final void interpol(final float time)
    {
        BackgroundStarsParticleSystem.logic(time);
    }
    
    public final void drawGame(final Canvas c)
    {
//        BackgroundStarsParticleSystem.draw(c);
    }
    
    public final void drawUI(final Canvas c)
    {
//        Log.i("GameActivity", "----------MENU-------------");

        Res.button_start.draw(c);
        Res.button_config.draw(c);
        Res.button_exit.draw(c);
        Res.fps_number.draw(c);
    }
    
}
